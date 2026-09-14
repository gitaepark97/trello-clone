import { apiClient } from './client'
import type { Board } from './types'

export interface CreateBoardInput {
  title: string
  description?: string
}

export interface UpdateBoardInput {
  title?: string
  description?: string
}

export async function fetchBoards(): Promise<Board[]> {
  const response = await apiClient.get<Board[]>('/boards')
  return response.data
}

export async function fetchBoard(boardId: number): Promise<Board> {
  const response = await apiClient.get<Board>(`/boards/${boardId}`)
  return response.data
}

export async function createBoard(input: CreateBoardInput): Promise<Board> {
  const response = await apiClient.post<Board>('/boards', input)
  return response.data
}

export async function updateBoard(boardId: number, input: UpdateBoardInput): Promise<Board> {
  const response = await apiClient.patch<Board>(`/boards/${boardId}`, input)
  return response.data
}

export async function deleteBoard(boardId: number): Promise<void> {
  await apiClient.delete(`/boards/${boardId}`)
}
