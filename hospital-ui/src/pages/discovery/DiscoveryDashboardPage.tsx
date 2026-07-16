import { useEffect, useState } from 'react'
import { discoveryDashboardApi } from '../../api/discoveryDashboard'
import { ApiError } from '../../api/client'
import type { DiscoveryDashboardSummary } from '../../api/discoveryTypes'
import { LoadingState } from '../../components/common/LoadingState'
import { ErrorState } from '../../components/common/ErrorState'
import { StatCard } from '../../components/common/StatCard'

export function DiscoveryDashboardPage() {
  const [summary, setSummary] = useState<DiscoveryDashboardSummary | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const load = () => {
    setLoading(true)
    setError(null)
    discoveryDashboardApi
      .summary()
      .then(setSummary)
      .catch((err: unknown) => {
        setError(err instanceof ApiError ? err.message : 'Failed to load the discovery dashboard')
      })
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
  }, [])

  if (loading) {
    return <LoadingState label="Loading discovery dashboard..." />
  }

  if (error || !summary) {
    return <ErrorState message={error ?? 'No data available'} onRetry={load} />
  }

  return (
    <div>
      <p className="mb-6 text-sm text-slate-500">
        Progress across every hospital discovery survey — projects, questionnaire coverage, and flagged risks.
      </p>
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard icon="🗂️" label="Total Projects" value={summary.totalProjects} />
        <StatCard icon="🚧" label="Active Surveys" value={summary.activeSurveys} />
        <StatCard icon="✅" label="Completed Surveys" value={summary.completedSurveys} />
        <StatCard icon="❓" label="Total Questions" value={summary.totalQuestions} />
        <StatCard icon="📝" label="Answered Questions" value={summary.answeredQuestions} />
        <StatCard icon="🔴" label="High Risks" value={summary.highRisks} />
        <StatCard icon="🟠" label="Medium Risks" value={summary.mediumRisks} />
        <StatCard icon="🟢" label="Low Risks" value={summary.lowRisks} />
      </div>
    </div>
  )
}
