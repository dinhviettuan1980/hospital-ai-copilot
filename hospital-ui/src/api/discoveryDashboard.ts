import { apiClient } from './client'
import type { DiscoveryDashboardSummary } from './discoveryTypes'

export const discoveryDashboardApi = {
  summary: () => apiClient.get<DiscoveryDashboardSummary>('/api/discovery/dashboard/summary'),
}
