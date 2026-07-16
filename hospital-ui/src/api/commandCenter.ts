import { apiClient } from './client'
import type { CommandCenterStatus } from './types'

export const commandCenterApi = {
  status: () => apiClient.get<CommandCenterStatus>('/api/command-center/status'),
}
