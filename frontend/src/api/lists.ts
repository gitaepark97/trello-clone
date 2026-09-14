import { apiClient } from './client'
import type { BoardListItem } from './types'

export interface CreateListInput {
  title: string
}

export interface UpdateListInput {
  title?: string
}

export async function fetchLists(boardId: number): Promise<BoardListItem[]> {
  const response = await apiClient.get<BoardListItem[]>(`/boards/${boardId}/lists`)
  return response.data
}

export async function createList(boardId: number, input: CreateListInput): Promise<BoardListItem> {
  const response = await apiClient.post<BoardListItem>(`/boards/${boardId}/lists`, input)
  return response.data
}

export async function updateList(listId: number, input: UpdateListInput): Promise<BoardListItem> {
  const response = await apiClient.patch<BoardListItem>(`/lists/${listId}`, input)
  return response.data
}

export async function deleteList(listId: number): Promise<void> {
  await apiClient.delete(`/lists/${listId}`)
}

export async function updateListPosition(listId: number, position: number): Promise<BoardListItem> {
  const response = await apiClient.patch<BoardListItem>(`/lists/${listId}/position`, { position })
  return response.data
}
