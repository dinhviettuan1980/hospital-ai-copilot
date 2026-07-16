import { apiClient, buildQuery } from './client'
import type { PageResponse } from './types'
import type {
  DiscoveryProject,
  DiscoveryProjectInput,
  DiscoveryProjectListParams,
  DiscoverySurveyExport,
} from './discoveryTypes'

export const discoveryProjectsApi = {
  list: (params: DiscoveryProjectListParams) =>
    apiClient.get<PageResponse<DiscoveryProject>>(`/api/discovery/projects${buildQuery(params)}`),
  get: (id: string) => apiClient.get<DiscoveryProject>(`/api/discovery/projects/${id}`),
  create: (input: DiscoveryProjectInput) =>
    apiClient.post<DiscoveryProject>('/api/discovery/projects', input),
  update: (id: string, input: DiscoveryProjectInput) =>
    apiClient.put<DiscoveryProject>(`/api/discovery/projects/${id}`, input),
  remove: (id: string) => apiClient.delete(`/api/discovery/projects/${id}`),

  export: (id: string) => apiClient.get<DiscoverySurveyExport>(`/api/discovery/projects/${id}/export`),
  import: (survey: DiscoverySurveyExport) =>
    apiClient.post<DiscoveryProject>('/api/discovery/projects/import', survey),
}
