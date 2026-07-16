import { apiClient, buildQuery } from './client'
import type { PageResponse, Patient, PatientInput } from './types'
import type { ListParams } from './departments'

export const patientsApi = {
  list: (params: ListParams) =>
    apiClient.get<PageResponse<Patient>>(`/api/patients${buildQuery(params)}`),
  get: (id: string) => apiClient.get<Patient>(`/api/patients/${id}`),
  create: (input: PatientInput) => apiClient.post<Patient>('/api/patients', input),
  update: (id: string, input: PatientInput) => apiClient.put<Patient>(`/api/patients/${id}`, input),
  remove: (id: string) => apiClient.delete(`/api/patients/${id}`),
}
