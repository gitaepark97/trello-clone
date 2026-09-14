import { useState, type FormEvent } from 'react'
import { useSortable } from '@dnd-kit/sortable'
import { useDroppable } from '@dnd-kit/core'
import { SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'
import { CardItem } from './CardItem'
import type { Card, BoardListItem } from '../api/types'

interface ListWithCards extends BoardListItem {
  cards: Card[]
}

interface ListColumnProps {
  list: ListWithCards
  onUpdateTitle: (title: string) => void
  onDelete: () => void
  onAddCard: (title: string) => void
  onCardClick: (card: Card) => void
}

export function ListColumn({ list, onUpdateTitle, onDelete, onAddCard, onCardClick }: ListColumnProps) {
  const [editingTitle, setEditingTitle] = useState(false)
  const [titleValue, setTitleValue] = useState(list.title)
  const [addingCard, setAddingCard] = useState(false)
  const [newCardTitle, setNewCardTitle] = useState('')

  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id: `list-${list.id}`,
    data: { type: 'list', id: list.id },
  })

  const { setNodeRef: setDroppableRef } = useDroppable({
    id: `list-drop-${list.id}`,
    data: { type: 'list-container', listId: list.id },
  })

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  }

  function submitTitle() {
    const trimmed = titleValue.trim()
    if (trimmed && trimmed !== list.title) {
      onUpdateTitle(trimmed)
    } else {
      setTitleValue(list.title)
    }
    setEditingTitle(false)
  }

  function handleAddCard(e: FormEvent) {
    e.preventDefault()
    const trimmed = newCardTitle.trim()
    if (!trimmed) return
    onAddCard(trimmed)
    setNewCardTitle('')
    setAddingCard(false)
  }

  return (
    <div ref={setNodeRef} style={style} className={`list-column${isDragging ? ' dragging' : ''}`}>
      <div className="list-column-header" {...attributes} {...listeners}>
        <div className="list-column-title">
          {editingTitle ? (
            <input
              value={titleValue}
              autoFocus
              onChange={(e) => setTitleValue(e.target.value)}
              onBlur={submitTitle}
              onKeyDown={(e) => {
                if (e.key === 'Enter') submitTitle()
                if (e.key === 'Escape') {
                  setTitleValue(list.title)
                  setEditingTitle(false)
                }
              }}
              onPointerDown={(e) => e.stopPropagation()}
            />
          ) : (
            <span onClick={() => setEditingTitle(true)}>{list.title}</span>
          )}
        </div>
        <button
          type="button"
          className="icon-btn"
          onPointerDown={(e) => e.stopPropagation()}
          onClick={onDelete}
        >
          삭제
        </button>
      </div>

      <SortableContext items={list.cards.map((c) => `card-${c.id}`)} strategy={verticalListSortingStrategy}>
        <div ref={setDroppableRef} className="list-cards">
          {list.cards.map((card) => (
            <CardItem key={card.id} card={card} onClick={() => onCardClick(card)} />
          ))}
        </div>
      </SortableContext>

      <div className="list-add-card">
        {addingCard ? (
          <form className="inline-form" onSubmit={handleAddCard}>
            <textarea
              value={newCardTitle}
              onChange={(e) => setNewCardTitle(e.target.value)}
              placeholder="카드 제목 입력"
              rows={2}
              autoFocus
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                  e.preventDefault()
                  handleAddCard(e)
                }
                if (e.key === 'Escape') setAddingCard(false)
              }}
            />
            <div className="inline-form-actions">
              <button type="submit" className="btn btn-primary">
                추가
              </button>
              <button type="button" className="btn btn-secondary" onClick={() => setAddingCard(false)}>
                취소
              </button>
            </div>
          </form>
        ) : (
          <button type="button" className="add-inline-btn" onClick={() => setAddingCard(true)}>
            + 카드 추가
          </button>
        )}
      </div>
    </div>
  )
}
