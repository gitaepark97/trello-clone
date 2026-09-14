import { useEffect, useState } from 'react'
import { BoardCard } from '../components/BoardCard'
import { BoardFormModal } from '../components/BoardFormModal'
import { ConfirmDialog } from '../components/ConfirmDialog'
import { createBoard, deleteBoard, fetchBoards, updateBoard } from '../api/boards'
import type { Board } from '../api/types'

export function BoardListPage() {
  const [boards, setBoards] = useState<Board[]>([])
  const [loading, setLoading] = useState(true)
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [editingBoard, setEditingBoard] = useState<Board | null>(null)
  const [deletingBoard, setDeletingBoard] = useState<Board | null>(null)

  useEffect(() => {
    void loadBoards()
  }, [])

  async function loadBoards() {
    setLoading(true)
    try {
      const data = await fetchBoards()
      setBoards(data)
    } finally {
      setLoading(false)
    }
  }

  async function handleCreate(input: { title: string; description?: string }) {
    const board = await createBoard(input)
    setBoards((prev) => [...prev, board])
  }

  async function handleUpdate(input: { title: string; description?: string }) {
    if (!editingBoard) return
    const board = await updateBoard(editingBoard.id, input)
    setBoards((prev) => prev.map((b) => (b.id === board.id ? board : b)))
  }

  async function handleDelete() {
    if (!deletingBoard) return
    await deleteBoard(deletingBoard.id)
    setBoards((prev) => prev.filter((b) => b.id !== deletingBoard.id))
    setDeletingBoard(null)
  }

  return (
    <div className="page">
      <header className="page-header">
        <h1>내 보드</h1>
        <button type="button" className="btn btn-primary" onClick={() => setShowCreateModal(true)}>
          + 새 보드
        </button>
      </header>

      {loading ? (
        <p className="empty-state">불러오는 중...</p>
      ) : boards.length === 0 ? (
        <p className="empty-state">아직 보드가 없습니다. 새 보드를 만들어 보세요.</p>
      ) : (
        <div className="board-grid">
          {boards.map((board) => (
            <BoardCard
              key={board.id}
              board={board}
              onEdit={setEditingBoard}
              onDelete={setDeletingBoard}
            />
          ))}
        </div>
      )}

      {showCreateModal && (
        <BoardFormModal onClose={() => setShowCreateModal(false)} onSubmit={handleCreate} />
      )}

      {editingBoard && (
        <BoardFormModal
          board={editingBoard}
          onClose={() => setEditingBoard(null)}
          onSubmit={handleUpdate}
        />
      )}

      {deletingBoard && (
        <ConfirmDialog
          title="보드 삭제"
          message={`"${deletingBoard.title}" 보드를 삭제하시겠습니까? 포함된 모든 리스트와 카드도 함께 삭제됩니다.`}
          onCancel={() => setDeletingBoard(null)}
          onConfirm={handleDelete}
        />
      )}
    </div>
  )
}
