import { useEffect, useState, type FormEvent } from 'react'
import { Link, useParams } from 'react-router-dom'
import {
  DndContext,
  DragOverlay,
  PointerSensor,
  closestCenter,
  pointerWithin,
  useSensor,
  useSensors,
  type CollisionDetection,
  type DragEndEvent,
  type DragStartEvent,
} from '@dnd-kit/core'
import { SortableContext, arrayMove, horizontalListSortingStrategy } from '@dnd-kit/sortable'
import { ListColumn } from '../components/ListColumn'
import { CardOverlay } from '../components/CardItem'
import { CardFormModal } from '../components/CardFormModal'
import { ConfirmDialog } from '../components/ConfirmDialog'
import { fetchBoard } from '../api/boards'
import { createList, deleteList, fetchLists, updateList, updateListPosition } from '../api/lists'
import { createCard, deleteCard, fetchCards, updateCard, updateCardPosition } from '../api/cards'
import { positionForIndex } from '../utils/position'
import type { Board, BoardListItem, Card } from '../api/types'

interface ListWithCards extends BoardListItem {
  cards: Card[]
}

export function BoardDetailPage() {
  const { boardId } = useParams<{ boardId: string }>()
  const id = Number(boardId)

  const [board, setBoard] = useState<Board | null>(null)
  const [lists, setLists] = useState<ListWithCards[]>([])
  const [loading, setLoading] = useState(true)
  const [addingList, setAddingList] = useState(false)
  const [newListTitle, setNewListTitle] = useState('')
  const [activeCard, setActiveCard] = useState<Card | null>(null)
  const [activeListId, setActiveListId] = useState<number | null>(null)
  const [editingCard, setEditingCard] = useState<Card | null>(null)
  const [deletingListId, setDeletingListId] = useState<number | null>(null)

  const sensors = useSensors(useSensor(PointerSensor, { activationConstraint: { distance: 5 } }))

  // Cards sit inside a list's droppable container, so a plain center-distance
  // strategy can match the (larger) container instead of the card under the
  // pointer. Prefer whichever droppable the pointer is actually over, and
  // prefer a 'card' match over its enclosing container when both qualify.
  const collisionDetection: CollisionDetection = (args) => {
    const pointerCollisions = pointerWithin(args)
    const cardCollisions = pointerCollisions.filter(
      (collision) =>
        args.droppableContainers.find((container) => container.id === collision.id)?.data.current
          ?.type === 'card',
    )
    if (cardCollisions.length > 0) {
      return cardCollisions
    }
    if (pointerCollisions.length > 0) {
      return pointerCollisions
    }
    return closestCenter(args)
  }

  useEffect(() => {
    void loadBoard()
  }, [id])

  async function loadBoard() {
    setLoading(true)
    try {
      const [boardData, listData] = await Promise.all([fetchBoard(id), fetchLists(id)])
      setBoard(boardData)
      const withCards = await Promise.all(
        listData.map(async (list) => ({ ...list, cards: await fetchCards(list.id) })),
      )
      setLists(withCards)
    } finally {
      setLoading(false)
    }
  }

  async function handleAddList(e: FormEvent) {
    e.preventDefault()
    const trimmed = newListTitle.trim()
    if (!trimmed) return
    const list = await createList(id, { title: trimmed })
    setLists((prev) => [...prev, { ...list, cards: [] }])
    setNewListTitle('')
    setAddingList(false)
  }

  async function handleUpdateListTitle(listId: number, title: string) {
    const updated = await updateList(listId, { title })
    setLists((prev) => prev.map((l) => (l.id === listId ? { ...l, title: updated.title } : l)))
  }

  async function handleDeleteList() {
    if (deletingListId === null) return
    await deleteList(deletingListId)
    setLists((prev) => prev.filter((l) => l.id !== deletingListId))
    setDeletingListId(null)
  }

  async function handleAddCard(listId: number, title: string) {
    const card = await createCard(listId, { title })
    setLists((prev) => prev.map((l) => (l.id === listId ? { ...l, cards: [...l.cards, card] } : l)))
  }

  async function handleUpdateCard(input: { title: string; description?: string }) {
    if (!editingCard) return
    const updated = await updateCard(editingCard.id, input)
    setLists((prev) =>
      prev.map((l) => ({
        ...l,
        cards: l.cards.map((c) => (c.id === updated.id ? updated : c)),
      })),
    )
  }

  async function handleDeleteCard() {
    if (!editingCard) return
    await deleteCard(editingCard.id)
    setLists((prev) =>
      prev.map((l) => ({ ...l, cards: l.cards.filter((c) => c.id !== editingCard.id) })),
    )
    setEditingCard(null)
  }

  function handleDragStart(event: DragStartEvent) {
    const { active } = event
    if (active.data.current?.type === 'card') {
      const listId = active.data.current.listId as number
      const cardId = active.data.current.id as number
      const list = lists.find((l) => l.id === listId)
      const card = list?.cards.find((c) => c.id === cardId) ?? null
      setActiveCard(card)
      setActiveListId(null)
    } else if (active.data.current?.type === 'list') {
      setActiveListId(active.data.current.id as number)
      setActiveCard(null)
    }
  }

  function handleDragEnd(event: DragEndEvent) {
    const { active, over } = event
    setActiveCard(null)
    setActiveListId(null)
    if (!over) return

    const activeData = active.data.current
    const overData = over.data.current

    if (activeData?.type === 'list') {
      const activeListId = activeData.id as number
      const overListId = overData?.type === 'list' ? (overData.id as number) : null
      if (overListId === null || activeListId === overListId) return

      const oldIndex = lists.findIndex((l) => l.id === activeListId)
      const newIndex = lists.findIndex((l) => l.id === overListId)
      if (oldIndex === -1 || newIndex === -1) return

      const reordered = arrayMove(lists, oldIndex, newIndex)
      const neighborPositions = reordered.filter((_, i) => i !== newIndex).map((l) => l.position)
      const newPosition = positionForIndex(neighborPositions, newIndex)
      reordered[newIndex] = { ...reordered[newIndex], position: newPosition }
      setLists(reordered)
      void updateListPosition(activeListId, newPosition).catch(() => void loadBoard())
      return
    }

    if (activeData?.type !== 'card') return

    const activeCardId = activeData.id as number
    const sourceListId = activeData.listId as number
    const targetListId =
      overData?.type === 'card' ? (overData.listId as number) : overData?.type === 'list-container' ? (overData.listId as number) : null
    if (targetListId === null) return

    const sourceListIndex = lists.findIndex((l) => l.id === sourceListId)
    const targetListIndex = lists.findIndex((l) => l.id === targetListId)
    if (sourceListIndex === -1 || targetListIndex === -1) return

    const activeCardIndex = lists[sourceListIndex].cards.findIndex((c) => c.id === activeCardId)
    if (activeCardIndex === -1) return

    if (sourceListId === targetListId) {
      // Reorder within the same list: compute indices on the original array so
      // arrayMove places the card before/after `over` regardless of drag direction.
      const overCardId = overData?.type === 'card' ? (overData.id as number) : null
      const overIndex =
        overCardId !== null
          ? lists[sourceListIndex].cards.findIndex((c) => c.id === overCardId)
          : lists[sourceListIndex].cards.length - 1
      if (overIndex === -1 || overIndex === activeCardIndex) return

      const reordered = arrayMove(lists[sourceListIndex].cards, activeCardIndex, overIndex)
      const newIndex = reordered.findIndex((c) => c.id === activeCardId)
      const neighborPositions = reordered.filter((_, i) => i !== newIndex).map((c) => c.position)
      const newPosition = positionForIndex(neighborPositions, newIndex)
      reordered[newIndex] = { ...reordered[newIndex], position: newPosition }

      const next = [...lists]
      next[sourceListIndex] = { ...next[sourceListIndex], cards: reordered }
      setLists(next)

      void updateCardPosition(activeCardId, targetListId, newPosition).catch(() => void loadBoard())
      return
    }

    // Move to a different list: insert at the position of `over` in the target
    // list (or at the end when dropped on the empty-list drop zone).
    const sourceCards = [...lists[sourceListIndex].cards]
    const [movedCard] = sourceCards.splice(activeCardIndex, 1)

    const targetCards = [...lists[targetListIndex].cards]
    const overCardId = overData?.type === 'card' ? (overData.id as number) : null
    const insertIndex = overCardId !== null ? targetCards.findIndex((c) => c.id === overCardId) : targetCards.length
    const safeInsertIndex = insertIndex === -1 ? targetCards.length : insertIndex

    const neighborPositions = targetCards.map((c) => c.position)
    const newPosition = positionForIndex(neighborPositions, safeInsertIndex)
    const updatedCard = { ...movedCard, listId: targetListId, position: newPosition }
    targetCards.splice(safeInsertIndex, 0, updatedCard)

    const next = [...lists]
    next[sourceListIndex] = { ...next[sourceListIndex], cards: sourceCards }
    next[targetListIndex] = { ...next[targetListIndex], cards: targetCards }
    setLists(next)

    void updateCardPosition(activeCardId, targetListId, newPosition).catch(() => void loadBoard())
  }

  if (loading) {
    return <p className="empty-state">불러오는 중...</p>
  }

  if (!board) {
    return <p className="empty-state">보드를 찾을 수 없습니다.</p>
  }

  return (
    <div className="board-detail">
      <header className="board-detail-header">
        <Link to="/" className="back-link">
          ← 내 보드
        </Link>
        <h1 className="board-detail-title">{board.title}</h1>
      </header>

      <DndContext
        sensors={sensors}
        collisionDetection={collisionDetection}
        onDragStart={handleDragStart}
        onDragEnd={handleDragEnd}
      >
        <div className="lists-container">
          <SortableContext items={lists.map((l) => `list-${l.id}`)} strategy={horizontalListSortingStrategy}>
            {lists.map((list) => (
              <ListColumn
                key={list.id}
                list={list}
                onUpdateTitle={(title) => void handleUpdateListTitle(list.id, title)}
                onDelete={() => setDeletingListId(list.id)}
                onAddCard={(title) => void handleAddCard(list.id, title)}
                onCardClick={setEditingCard}
              />
            ))}
          </SortableContext>

          <div className="add-list-column">
            {addingList ? (
              <form className="inline-form" onSubmit={handleAddList}>
                <input
                  value={newListTitle}
                  onChange={(e) => setNewListTitle(e.target.value)}
                  placeholder="리스트 제목 입력"
                  autoFocus
                  onKeyDown={(e) => {
                    if (e.key === 'Escape') setAddingList(false)
                  }}
                />
                <div className="inline-form-actions">
                  <button type="submit" className="btn btn-primary">
                    추가
                  </button>
                  <button type="button" className="btn btn-secondary" onClick={() => setAddingList(false)}>
                    취소
                  </button>
                </div>
              </form>
            ) : (
              <button type="button" className="add-inline-btn" onClick={() => setAddingList(true)}>
                + 리스트 추가
              </button>
            )}
          </div>
        </div>

        <DragOverlay>
          {activeCard ? <CardOverlay card={activeCard} /> : null}
          {activeListId ? (
            <div className="list-column dragging" style={{ width: 260 }}>
              <div className="list-column-header">
                <div className="list-column-title">
                  {lists.find((l) => l.id === activeListId)?.title}
                </div>
              </div>
            </div>
          ) : null}
        </DragOverlay>
      </DndContext>

      {editingCard && (
        <CardFormModal
          card={editingCard}
          onClose={() => setEditingCard(null)}
          onSubmit={handleUpdateCard}
          onDelete={handleDeleteCard}
        />
      )}

      {deletingListId !== null && (
        <ConfirmDialog
          title="리스트 삭제"
          message="이 리스트를 삭제하시겠습니까? 포함된 모든 카드도 함께 삭제됩니다."
          onCancel={() => setDeletingListId(null)}
          onConfirm={handleDeleteList}
        />
      )}
    </div>
  )
}
