import { useNavigate } from 'react-router-dom'
import type { Board } from '../api/types'

interface BoardCardProps {
  board: Board
  onEdit: (board: Board) => void
  onDelete: (board: Board) => void
}

export function BoardCard({ board, onEdit, onDelete }: BoardCardProps) {
  const navigate = useNavigate()

  return (
    <div className="board-card" onClick={() => navigate(`/boards/${board.id}`)}>
      <div className="board-card-title">{board.title}</div>
      {board.description && <div className="board-card-description">{board.description}</div>}
      <div className="board-card-actions" onClick={(e) => e.stopPropagation()}>
        <button type="button" className="icon-btn" onClick={() => onEdit(board)} aria-label="보드 수정">
          수정
        </button>
        <button type="button" className="icon-btn" onClick={() => onDelete(board)} aria-label="보드 삭제">
          삭제
        </button>
      </div>
    </div>
  )
}
