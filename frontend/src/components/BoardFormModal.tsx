import { useState, type FormEvent } from 'react'
import { Modal } from './Modal'
import type { Board } from '../api/types'

interface BoardFormModalProps {
  board?: Board
  onClose: () => void
  onSubmit: (input: { title: string; description?: string }) => Promise<void>
}

export function BoardFormModal({ board, onClose, onSubmit }: BoardFormModalProps) {
  const [title, setTitle] = useState(board?.title ?? '')
  const [description, setDescription] = useState(board?.description ?? '')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    if (!title.trim()) {
      setError('제목을 입력해 주세요.')
      return
    }
    setSubmitting(true)
    setError(null)
    try {
      await onSubmit({ title: title.trim(), description: description.trim() })
      onClose()
    } catch {
      setError('저장에 실패했습니다. 다시 시도해 주세요.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Modal title={board ? '보드 수정' : '새 보드 만들기'} onClose={onClose}>
      <form className="form" onSubmit={handleSubmit}>
        <label className="form-field">
          <span>제목</span>
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="보드 제목"
            autoFocus
          />
        </label>
        <label className="form-field">
          <span>설명 (선택)</span>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="보드 설명"
            rows={3}
          />
        </label>
        {error && <p className="form-error">{error}</p>}
        <div className="form-actions">
          <button type="button" className="btn btn-secondary" onClick={onClose}>
            취소
          </button>
          <button type="submit" className="btn btn-primary" disabled={submitting}>
            {board ? '수정' : '생성'}
          </button>
        </div>
      </form>
    </Modal>
  )
}
