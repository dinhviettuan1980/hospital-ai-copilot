import { apiClient, buildQuery } from './client'
import type { Department, DepartmentInput, ListParams, PageResponse } from './types'

export type { ListParams } from './types'

export const departmentsApi = {
  list: (params: ListParams) =>
    apiClient.get<PageResponse<Department>>(`/api/departments${buildQuery(params)}`),
  get: (id: string) => apiClient.get<Department>(`/api/departments/${id}`),
  create: (input: DepartmentInput) => apiClient.post<Department>('/api/departments', input),
  update: (id: string, input: DepartmentInput) =>
    apiClient.put<Department>(`/api/departments/${id}`, input),
  remove: (id: string) => apiClient.delete(`/api/departments/${id}`),
}
