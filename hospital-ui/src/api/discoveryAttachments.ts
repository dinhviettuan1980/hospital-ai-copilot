import { apiClient, fileUrl } from './client'
import type { DiscoveryAttachment } from './discoveryTypes'

export const discoveryAttachmentsApi = {
  listByProject: (projectId: string) =>
    apiClient.get<DiscoveryAttachment[]>(`/api/discovery/projects/${projectId}/attachments`),

  upload: (projectId: string, file: File, questionId?: string) => {
    const form = new FormData()
    if (questionId) form.set('questionId', questionId)
    form.set('file', file)
    return apiClient.postForm<DiscoveryAttachment>(`/api/discovery/projects/${projectId}/attachments`, form)
  },

  downloadUrl: (id: string) => fileUrl(`/api/discovery/attachments/${id}/download`),

  remove: (id: string) => apiClient.delete(`/api/discovery/attachments/${id}`),
}
