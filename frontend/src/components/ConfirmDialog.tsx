import { Modal } from './Modal'

interface ConfirmDialogProps {
  title: string
  message: string
  onCancel: () => void
  onConfirm: () => void
}

export function ConfirmDialog({ title, message, onCancel, onConfirm }: ConfirmDialogProps) {
  return (
    <Modal title={title} onClose={onCancel}>
      <p className="confirm-message">{message}</p>
      <div className="form-actions">
        <button type="button" className="btn btn-secondary" onClick={onCancel}>
          취소
        </button>
        <button type="button" className="btn btn-danger" onClick={onConfirm}>
          삭제
        </button>
      </div>
    </Modal>
  )
}
