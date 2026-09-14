import { useSortable } from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'
import type { Card } from '../api/types'

interface CardItemProps {
  card: Card
  onClick: () => void
}

export function CardItem({ card, onClick }: CardItemProps) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id: `card-${card.id}`,
    data: { type: 'card', id: card.id, listId: card.listId },
  })

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  }

  return (
    <div
      ref={setNodeRef}
      style={style}
      {...attributes}
      {...listeners}
      className={`card-item${isDragging ? ' dragging' : ''}`}
      onClick={onClick}
    >
      <div className="card-item-title">{card.title}</div>
      {card.description && <div className="card-item-description">{card.description}</div>}
    </div>
  )
}

export function CardOverlay({ card }: { card: Card }) {
  return (
    <div className="card-overlay">
      <div className="card-item-title">{card.title}</div>
    </div>
  )
}
