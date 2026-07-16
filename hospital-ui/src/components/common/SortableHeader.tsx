interface SortableHeaderProps {
  label: string
  field: string
  activeField?: string
  direction: 'asc' | 'desc'
  onSort: (field: string) => void
}

export function SortableHeader({ label, field, activeField, direction, onSort }: SortableHeaderProps) {
  const isActive = activeField === field
  return (
    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
      <button
        type="button"
        onClick={() => onSort(field)}
        className="flex items-center gap-1 hover:text-slate-800"
      >
        {label}
        {isActive && <span aria-hidden="true">{direction === 'asc' ? '▲' : '▼'}</span>}
      </button>
    </th>
  )
}
