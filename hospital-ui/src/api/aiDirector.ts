import { apiClient } from './client'
import type { AiDirectorResponse } from './types'

export const aiDirectorApi = {
  ask: (question: string) => apiClient.post<AiDirectorResponse>('/api/ai-director/ask', { question }),
}
