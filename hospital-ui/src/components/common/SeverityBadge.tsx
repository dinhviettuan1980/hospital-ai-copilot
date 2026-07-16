import type { AlertSeverity } from '../../api/types'

const STYLES: Record<AlertSeverity, string> = {
  GREEN: 'bg-green-100 text-green-800',
  YELLOW: 'bg-amber-100 text-amber-800',
  RED: 'bg-red-100 text-red-800',
}

const LABELS: Record<AlertSeverity, string> = {
  GREEN: 'Normal',
  YELLOW: 'Warning',
  RED: 'Critical',
}

export function SeverityBadge({ severity }: { severity: AlertSeverity }) {
  return (
    <span className={`inline-flex items-center rounded-full px-3 py-1 text-sm font-semibold ${STYLES[severity]}`}>
      {LABELS[severity]}
    </span>
  )
}
