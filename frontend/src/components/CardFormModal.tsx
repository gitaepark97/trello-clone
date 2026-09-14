import { useState, type FormEvent } from 'react'
import { Modal } from './Modal'
import type { Card } from '../api/types'

interface CardFormModalProps {
  card: Card
  onClose: () => void
  onSubmit: (input: { title: string; description?: string }) => Promise<void>
  onDelete: () => void
}

export function CardFormModal({ card, onClose, onSubmit, onDelete }: CardFormModalProps) {
  const [title, setTitle] = useState(card.title)
  const [description, setDescription] = useState(card.description ?? '')
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
    <Modal title="카드 수정" onClose={onClose}>
      <form className="form" onSubmit={handleSubmit}>
        <label className="form-field">
          <span>제목</span>
          <input type="text" value={title} onChange={(e) => setTitle(e.target.value)} autoFocus />
        </label>
        <label className="form-field">
          <span>설명 (선택)</span>
          <textarea value={description} onChange={(e) => setDescription(e.target.value)} rows={4} />
        </label>
        {error && <p className="form-error">{error}</p>}
        <div className="form-actions" style={{ justifyContent: 'space-between' }}>
          <button type="button" className="btn btn-danger" onClick={onDelete}>
            삭제
          </button>
          <div className="form-actions">
            <button type="button" className="btn btn-secondary" onClick={onClose}>
              취소
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              저장
            </button>
          </div>
        </div>
      </form>
    </Modal>
  )
}
