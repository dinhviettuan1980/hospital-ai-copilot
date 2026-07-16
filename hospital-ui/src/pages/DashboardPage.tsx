import { useEffect, useState } from 'react'
import { dashboardApi } from '../api/dashboard'
import { ApiError } from '../api/client'
import type { ExecutiveSummary } from '../api/types'
import { LoadingState } from '../components/common/LoadingState'
import { ErrorState } from '../components/common/ErrorState'
import { StatCard } from '../components/common/StatCard'
import { formatCurrency, formatMinutes, formatPercent } from '../lib/format'

export function DashboardPage() {
  const [summary, setSummary] = useState<ExecutiveSummary | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const load = () => {
    setLoading(true)
    setError(null)
    dashboardApi
      .executiveSummary()
      .then(setSummary)
      .catch((err: unknown) => {
        setError(err instanceof ApiError ? err.message : 'Failed to load the executive summary')
      })
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
  }, [])

  if (loading) {
    return <LoadingState label="Loading executive dashboard..." />
  }

  if (error || !summary) {
    return <ErrorState message={error ?? 'No data available'} onRetry={load} />
  }

  return (
    <div>
      <p className="mb-6 text-sm text-slate-500">
        A real-time view of today's hospital operations — generated live from PostgreSQL.
      </p>
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard icon="🧑‍🤝‍🧑" label="Today's Patients" value={summary.todaysPatients} />
        <StatCard icon="📅" label="Today's Visits" value={summary.todaysVisits} />
        <StatCard icon="🛏️" label="Bed Occupancy" value={formatPercent(summary.bedOccupancyRate)} />
        <StatCard icon="🚨" label="ICU Occupancy" value={formatPercent(summary.icuOccupancyRate)} />
        <StatCard icon="🔪" label="Today's Surgeries" value={summary.todaysSurgeries} />
        <StatCard icon="⛑️" label="Emergency Cases" value={summary.emergencyCases} />
        <StatCard icon="⏱️" label="Average Waiting Time" value={formatMinutes(summary.averageWaitingMinutes)} />
        <StatCard icon="💵" label="Today's Revenue" value={formatCurrency(summary.todaysRevenue)} />
      </div>
    </div>
  )
}
