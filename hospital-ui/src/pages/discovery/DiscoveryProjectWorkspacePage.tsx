import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { discoveryProjectsApi } from '../../api/discoveryProjects'
import { discoverySurveyApi } from '../../api/discoverySurvey'
import { ApiError } from '../../api/client'
import type {
  DiscoveryProject,
  DiscoveryQuestionWithAnswer,
  DiscoverySectionProgress,
} from '../../api/discoveryTypes'
import { LoadingState } from '../../components/common/LoadingState'
import { ErrorState } from '../../components/common/ErrorState'
import { ProgressBar } from '../../components/common/ProgressBar'
import { QuestionCard } from '../../components/discovery/QuestionCard'

export function DiscoveryProjectWorkspacePage() {
  const { projectId } = useParams<{ projectId: string }>()

  const [project, setProject] = useState<DiscoveryProject | null>(null)
  const [sections, setSections] = useState<DiscoverySectionProgress[]>([])
  const [selectedSectionId, setSelectedSectionId] = useState<string | null>(null)
  const [questions, setQuestions] = useState<DiscoveryQuestionWithAnswer[] | null>(null)

  const [loading, setLoading] = useState(true)
  const [questionsLoading, setQuestionsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [exporting, setExporting] = useState(false)

  const loadOverview = () => {
    if (!projectId) return
    setLoading(true)
    setError(null)
    Promise.all([discoveryProjectsApi.get(projectId), discoverySurveyApi.listSections(projectId)])
      .then(([projectData, sectionsData]) => {
        setProject(projectData)
        setSections(sectionsData)
        setSelectedSectionId((current) => current ?? sectionsData[0]?.id ?? null)
      })
      .catch((err: unknown) => {
        setError(err instanceof ApiError ? err.message : 'Failed to load the discovery project')
      })
      .finally(() => setLoading(false))
  }

  const refreshSectionsOnly = () => {
    if (!projectId) return
    discoverySurveyApi.listSections(projectId).then(setSections).catch(() => undefined)
    discoveryProjectsApi.get(projectId).then(setProject).catch(() => undefined)
  }

  useEffect(() => {
    loadOverview()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [projectId])

  useEffect(() => {
    if (!projectId || !selectedSectionId) return
    setQuestionsLoading(true)
    discoverySurveyApi
      .listQuestions(projectId, selectedSectionId)
      .then(setQuestions)
      .catch(() => setQuestions([]))
      .finally(() => setQuestionsLoading(false))
  }, [projectId, selectedSectionId])

  const handleExport = async () => {
    if (!projectId || !project) return
    setExporting(true)
    try {
      const survey = await discoveryProjectsApi.export(projectId)
      const blob = new Blob([JSON.stringify(survey, null, 2)], { type: 'application/json' })
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `${project.projectName.replace(/\s+/g, '_')}_export.json`
      link.click()
      URL.revokeObjectURL(url)
    } finally {
      setExporting(false)
    }
  }

  if (loading) {
    return <LoadingState label="Loading discovery project..." />
  }

  if (error || !project) {
    return <ErrorState message={error ?? 'Project not found'} onRetry={loadOverview} />
  }

  const selectedSection = sections.find((s) => s.id === selectedSectionId)

  return (
    <div>
      <div className="mb-4 flex items-center gap-2 text-sm text-slate-500">
        <Link to="/discovery/projects" className="hover:underline">
          Discovery Projects
        </Link>
        <span>/</span>
        <span className="text-slate-700">{project.projectName}</span>
      </div>

      <div className="mb-6 rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
        <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
          <div>
            <h2 className="text-lg font-semibold text-slate-900">{project.projectName}</h2>
            <p className="text-sm text-slate-500">{project.hospitalName}</p>
          </div>
          <button
            type="button"
            onClick={handleExport}
            disabled={exporting}
            className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50 disabled:opacity-50"
          >
            {exporting ? 'Exporting...' : '⬇ Export JSON'}
          </button>
        </div>
        <div className="mt-4 max-w-md">
          <ProgressBar percent={project.progressPercent} label="Overall survey progress" />
        </div>
      </div>

      <div className="flex flex-col gap-6 lg:flex-row">
        <aside className="w-full shrink-0 lg:w-72">
          <nav className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
            {sections.map((section) => (
              <button
                key={section.id}
                type="button"
                onClick={() => setSelectedSectionId(section.id)}
                className={`flex w-full items-center justify-between gap-2 border-b border-slate-100 px-4 py-3 text-left text-sm last:border-b-0 ${
                  section.id === selectedSectionId ? 'bg-slate-900 text-white' : 'text-slate-700 hover:bg-slate-50'
                }`}
              >
                <span className="truncate">{section.name}</span>
                <span
                  className={`shrink-0 text-xs ${
                    section.id === selectedSectionId ? 'text-slate-300' : 'text-slate-400'
                  }`}
                >
                  {section.answeredQuestions}/{section.totalQuestions}
                </span>
              </button>
            ))}
          </nav>
        </aside>

        <main className="flex-1">
          {selectedSection && (
            <div className="mb-4">
              <h3 className="font-semibold text-slate-900">{selectedSection.name}</h3>
              {selectedSection.description && (
                <p className="text-sm text-slate-500">{selectedSection.description}</p>
              )}
            </div>
          )}

          {questionsLoading && <LoadingState label="Loading questions..." />}

          {!questionsLoading && questions && (
            <div className="flex flex-col gap-3">
              {questions.map((question) => (
                <QuestionCard
                  key={question.id}
                  projectId={projectId as string}
                  question={question}
                  onAnswered={refreshSectionsOnly}
                />
              ))}
            </div>
          )}
        </main>
      </div>
    </div>
  )
}
