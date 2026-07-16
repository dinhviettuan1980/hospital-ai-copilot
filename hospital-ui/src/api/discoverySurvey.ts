import { apiClient } from './client'
import type {
  DiscoveryAnswer,
  DiscoveryAnswerInput,
  DiscoveryQuestionWithAnswer,
  DiscoverySectionProgress,
} from './discoveryTypes'

export const discoverySurveyApi = {
  listSections: (projectId: string) =>
    apiClient.get<DiscoverySectionProgress[]>(`/api/discovery/projects/${projectId}/sections`),

  listQuestions: (projectId: string, sectionId: string) =>
    apiClient.get<DiscoveryQuestionWithAnswer[]>(
      `/api/discovery/projects/${projectId}/sections/${sectionId}/questions`,
    ),

  saveAnswer: (projectId: string, questionId: string, input: DiscoveryAnswerInput) =>
    apiClient.put<DiscoveryAnswer>(`/api/discovery/projects/${projectId}/questions/${questionId}/answer`, input),
}
