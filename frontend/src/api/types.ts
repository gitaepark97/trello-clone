export interface Board {
  id: number
  title: string
  description: string | null
  ownerId: number | null
  createdAt: string
  updatedAt: string
}

export interface BoardListItem {
  id: number
  boardId: number
  title: string
  position: number
  createdAt: string
  updatedAt: string
}

export interface Card {
  id: number
  listId: number
  title: string
  description: string | null
  position: number
  createdAt: string
  updatedAt: string
}
