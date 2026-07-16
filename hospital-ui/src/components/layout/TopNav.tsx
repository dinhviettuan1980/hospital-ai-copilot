interface TopNavProps {
  onToggleSidebar: () => void
  title: string
}

export function TopNav({ onToggleSidebar, title }: TopNavProps) {
  return (
    <header className="sticky top-0 z-20 flex h-16 items-center gap-4 border-b border-slate-200 bg-white px-4 sm:px-6">
      <button
        type="button"
        onClick={onToggleSidebar}
        aria-label="Toggle navigation"
        className="rounded-md p-2 text-slate-500 hover:bg-slate-100 md:hidden"
      >
        ☰
      </button>
      <h1 className="text-lg font-semibold text-slate-900">{title}</h1>
    </header>
  )
}
