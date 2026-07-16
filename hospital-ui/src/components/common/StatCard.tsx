interface StatCardProps {
  label: string
  value: number | string
  icon?: string
  hint?: string
}

export function StatCard({ label, value, icon, hint }: StatCardProps) {
  return (
    <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex items-center justify-between">
        <p className="text-sm font-medium text-slate-500">{label}</p>
        {icon && (
          <span className="text-xl" aria-hidden="true">
            {icon}
          </span>
        )}
      </div>
      <p className="mt-2 text-3xl font-semibold text-slate-900">{value}</p>
      {hint && <p className="mt-1 text-xs text-slate-400">{hint}</p>}
    </div>
  )
}
