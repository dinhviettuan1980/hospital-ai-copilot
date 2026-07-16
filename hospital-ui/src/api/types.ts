export type Gender = 'MALE' | 'FEMALE' | 'OTHER'

export type VisitStatus = 'SCHEDULED' | 'COMPLETED' | 'CANCELLED'

export interface Department {
  id: string
  name: string
  code: string
  description: string | null
  createdAt: string
  updatedAt: string
}

export interface DepartmentInput {
  name: string
  code: string
  description: string
}

export interface Patient {
  id: string
  firstName: string
  lastName: string
  fullName: string
  dateOfBirth: string
  gender: Gender
  phone: string | null
  email: string | null
  address: string | null
  createdAt: string
  updatedAt: string
}

export interface PatientInput {
  firstName: string
  lastName: string
  dateOfBirth: string
  gender: Gender
  phone: string
  email: string
  address: string
}

export interface Visit {
  id: string
  patient: { id: string; fullName: string }
  department: { id: string; name: string; code: string }
  visitDate: string
  reason: string
  status: VisitStatus
  notes: string | null
  createdAt: string
  updatedAt: string
}

export interface VisitInput {
  patientId: string
  departmentId: string
  visitDate: string
  reason: string
  status: VisitStatus
  notes: string
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface DashboardSummary {
  totalPatients: number
  totalDepartments: number
  totalVisits: number
  todaysVisits: number
}

export interface ApiErrorBody {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
  details: string[]
}

/** Canonical shape of the list query params every paginated endpoint accepts. */
export interface ListParams {
  q?: string
  page?: number
  size?: number
  sortBy?: string
  sortDir?: 'asc' | 'desc'
}

// --- Sprint 2: Executive Dashboard ----------------------------------------

export interface ExecutiveSummary {
  todaysPatients: number
  todaysVisits: number
  bedOccupancyRate: number
  icuOccupancyRate: number
  todaysSurgeries: number
  emergencyCases: number
  averageWaitingMinutes: number
  todaysRevenue: number
}

// --- Sprint 2: Command Center ---------------------------------------------

export type AlertSeverity = 'GREEN' | 'YELLOW' | 'RED'

export interface Alert {
  severity: AlertSeverity
  title: string
  message: string
}

export interface CommandCenterStatus {
  overallStatus: AlertSeverity
  alerts: Alert[]
}

// --- Sprint 2: AI Director (demo engine, no LLM) ---------------------------

export interface AiDirectorResponse {
  answer: string
  intent: string
  data: Record<string, unknown> | null
}

// --- Sprint 2: Knowledge Center --------------------------------------------

export interface DocumentCategory {
  id: string
  name: string
}

export interface KnowledgeDocument {
  id: string
  title: string
  category: DocumentCategory
  fileName: string
  contentType: string
  fileSize: number
  createdAt: string
}

export interface KnowledgeDocumentListParams extends ListParams {
  title?: string
  categoryId?: string
}
