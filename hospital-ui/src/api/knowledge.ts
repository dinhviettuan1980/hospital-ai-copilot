import { apiClient, buildQuery, fileUrl } from './client'
import type {
  DocumentCategory,
  KnowledgeDocument,
  KnowledgeDocumentListParams,
  PageResponse,
} from './types'

export const knowledgeApi = {
  listCategories: () => apiClient.get<DocumentCategory[]>('/api/knowledge/categories'),
  createCategory: (name: string) =>
    apiClient.post<DocumentCategory>('/api/knowledge/categories', { name }),

  listDocuments: (params: KnowledgeDocumentListParams) =>
    apiClient.get<PageResponse<KnowledgeDocument>>(`/api/knowledge/documents${buildQuery(params)}`),

  upload: (title: string, categoryId: string, file: File) => {
    const form = new FormData()
    form.set('title', title)
    form.set('categoryId', categoryId)
    form.set('file', file)
    return apiClient.postForm<KnowledgeDocument>('/api/knowledge/documents', form)
  },

  downloadUrl: (id: string) => fileUrl(`/api/knowledge/documents/${id}/download`),

  remove: (id: string) => apiClient.delete(`/api/knowledge/documents/${id}`),
}
