import { apiClient, buildQuery } from './client'
import type { PageResponse, Visit, VisitInput, VisitStatus } from './types'
import type { ListParams } from './departments'

export interface VisitListParams extends ListParams {
  departmentId?: string
  patientId?: string
  status?: VisitStatus
}

export const visitsApi = {
  list: (params: VisitListParams) =>
    apiClient.get<PageResponse<Visit>>(`/api/visits${buildQuery(params)}`),
  get: (id: string) => apiClient.get<Visit>(`/api/visits/${id}`),
  create: (input: VisitInput) => apiClient.post<Visit>('/api/visits', input),
  update: (id: string, input: VisitInput) => apiClient.put<Visit>(`/api/visits/${id}`, input),
  remove: (id: string) => apiClient.delete(`/api/visits/${id}`),
}
