import { apiClient } from './client'
import type { DashboardSummary, ExecutiveSummary } from './types'

export const dashboardApi = {
  summary: () => apiClient.get<DashboardSummary>('/api/dashboard/summary'),
  executiveSummary: () => apiClient.get<ExecutiveSummary>('/api/dashboard/executive-summary'),
}
