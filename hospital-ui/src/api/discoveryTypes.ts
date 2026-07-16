import type { ListParams } from './types'

export type DiscoveryProjectStatus = 'DRAFT' | 'IN_PROGRESS' | 'COMPLETED'

export type DiscoveryAnswerType =
  | 'TEXT'
  | 'NUMBER'
  | 'DATE'
  | 'YES_NO'
  | 'SINGLE_CHOICE'
  | 'MULTIPLE_CHOICE'
  | 'RATING'
  | 'FILE_ATTACHMENT'
  | 'URL'

export type DiscoveryRiskLevel = 'LOW' | 'MEDIUM' | 'HIGH'

export interface DiscoveryProject {
  id: string
  projectName: string
  hospitalName: string
  contactPerson: string | null
  contactEmail: string | null
  contactPhone: string | null
  surveyDate: string | null
  status: DiscoveryProjectStatus
  notes: string | null
  progressPercent: number
  createdAt: string
  updatedAt: string
}

export interface DiscoveryProjectInput {
  projectName: string
  hospitalName: string
  contactPerson: string
  contactEmail: string
  contactPhone: string
  surveyDate: string
  status: DiscoveryProjectStatus
  notes: string
}

export interface DiscoverySectionProgress {
  id: string
  code: string
  name: string
  description: string | null
  displayOrder: number
  totalQuestions: number
  answeredQuestions: number
  percent: number
}

export interface DiscoveryAnswer {
  answerValue: string | null
  comment: string | null
  riskLevel: DiscoveryRiskLevel | null
  updatedAt: string
}

export interface DiscoveryAttachment {
  id: string
  questionId: string | null
  fileName: string
  contentType: string
  fileSize: number
  createdAt: string
}

export interface DiscoveryQuestionWithAnswer {
  id: string
  code: string
  title: string
  description: string | null
  answerType: DiscoveryAnswerType
  options: string[]
  displayOrder: number
  answer: DiscoveryAnswer | null
  attachments: DiscoveryAttachment[]
}

export interface DiscoveryAnswerInput {
  answerValue: string | null
  comment: string | null
  riskLevel: DiscoveryRiskLevel | null
}

export interface DiscoveryDashboardSummary {
  totalProjects: number
  activeSurveys: number
  completedSurveys: number
  totalQuestions: number
  answeredQuestions: number
  highRisks: number
  mediumRisks: number
  lowRisks: number
}

// --- Export / Import -------------------------------------------------------

export interface DiscoveryProjectExport {
  projectName: string
  hospitalName: string
  contactPerson: string | null
  contactEmail: string | null
  contactPhone: string | null
  surveyDate: string | null
  status: DiscoveryProjectStatus
  notes: string | null
}

export interface DiscoveryAnswerExport {
  value: string | null
  comment: string | null
  riskLevel: DiscoveryRiskLevel | null
}

export interface DiscoveryAttachmentExport {
  fileName: string
  contentType: string
  fileSize: number
}

export interface DiscoveryQuestionExport {
  code: string
  title: string
  description: string | null
  answerType: DiscoveryAnswerType
  options: string[]
  answer: DiscoveryAnswerExport | null
  attachments: DiscoveryAttachmentExport[]
}

export interface DiscoverySectionExport {
  code: string
  name: string
  description: string | null
  displayOrder: number
  questions: DiscoveryQuestionExport[]
}

export interface DiscoveryProgress {
  totalQuestions: number
  answeredQuestions: number
  percent: number
}

export interface DiscoverySurveyExport {
  exportVersion: number
  exportedAt: string
  project: DiscoveryProjectExport
  progress: DiscoveryProgress
  sections: DiscoverySectionExport[]
}

export type DiscoveryProjectListParams = ListParams
