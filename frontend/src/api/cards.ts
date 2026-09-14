import { apiClient } from './client'
import type { Card } from './types'

export interface CreateCardInput {
  title: string
  description?: string
}

export interface UpdateCardInput {
  title?: string
  description?: string
}

export async function fetchCards(listId: number): Promise<Card[]> {
  const response = await apiClient.get<Card[]>(`/lists/${listId}/cards`)
  return response.data
}

export async function createCard(listId: number, input: CreateCardInput): Promise<Card> {
  const response = await apiClient.post<Card>(`/lists/${listId}/cards`, input)
  return response.data
}

export async function updateCard(cardId: number, input: UpdateCardInput): Promise<Card> {
  const response = await apiClient.patch<Card>(`/cards/${cardId}`, input)
  return response.data
}

export async function deleteCard(cardId: number): Promise<void> {
  await apiClient.delete(`/cards/${cardId}`)
}

export async function updateCardPosition(
  cardId: number,
  listId: number,
  position: number,
): Promise<Card> {
  const response = await apiClient.patch<Card>(`/cards/${cardId}/position`, { listId, position })
  return response.data
}
