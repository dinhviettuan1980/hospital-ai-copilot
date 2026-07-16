import { useEffect, useState } from 'react'
import { commandCenterApi } from '../api/commandCenter'
import { ApiError } from '../api/client'
import type { CommandCenterStatus } from '../api/types'
import { LoadingState } from '../components/common/LoadingState'
import { ErrorState } from '../components/common/ErrorState'
import { SeverityBadge } from '../components/common/SeverityBadge'

const BORDER_BY_SEVERITY = {
  GREEN: 'border-green-200',
  YELLOW: 'border-amber-300',
  RED: 'border-red-300',
} as const

export function CommandCenterPage() {
  const [status, setStatus] = useState<CommandCenterStatus | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const load = () => {
    setLoading(true)
    setError(null)
    commandCenterApi
      .status()
      .then(setStatus)
      .catch((err: unknown) => {
        setError(err instanceof ApiError ? err.message : 'Failed to load Command Center status')
      })
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
  }, [])

  if (loading) {
    return <LoadingState label="Checking hospital status..." />
  }

  if (error || !status) {
    return <ErrorState message={error ?? 'No data available'} onRetry={load} />
  }

  return (
    <div>
      <div className="mb-6 flex flex-col items-start justify-between gap-3 rounded-lg border border-slate-200 bg-white p-5 shadow-sm sm:flex-row sm:items-center">
        <div>
          <p className="text-sm font-medium text-slate-500">Overall Hospital Status</p>
          <p className="text-sm text-slate-400">Rule-based operational status, refreshed on demand.</p>
        </div>
        <SeverityBadge severity={status.overallStatus} />
      </div>

      {status.alerts.length === 0 ? (
        <div className="rounded-lg border border-green-200 bg-green-50 p-6 text-center text-green-800">
          ✅ All systems normal. No active alerts.
        </div>
      ) : (
        <div className="flex flex-col gap-3">
          {status.alerts.map((alert) => (
            <div
              key={alert.title}
              className={`rounded-lg border-2 bg-white p-4 shadow-sm ${BORDER_BY_SEVERITY[alert.severity]}`}
            >
              <div className="mb-1 flex items-center justify-between gap-3">
                <h3 className="font-semibold text-slate-900">{alert.title}</h3>
                <SeverityBadge severity={alert.severity} />
              </div>
              <p className="text-sm text-slate-600">{alert.message}</p>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
