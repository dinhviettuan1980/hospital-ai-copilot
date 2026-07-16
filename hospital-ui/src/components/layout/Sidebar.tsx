import { NavLink } from 'react-router-dom'

const COPILOT_ITEMS = [
  { to: '/', label: 'Dashboard', icon: '📊', end: true },
  { to: '/command-center', label: 'Command Center', icon: '🚦' },
  { to: '/ai-director', label: 'AI Director', icon: '💬' },
  { to: '/knowledge-center', label: 'Knowledge Center', icon: '📚' },
]

const RECORDS_ITEMS = [
  { to: '/departments', label: 'Departments', icon: '🏥' },
  { to: '/patients', label: 'Patients', icon: '🧑‍⚕️' },
  { to: '/visits', label: 'Visits', icon: '📅' },
]

const DISCOVERY_ITEMS = [
  { to: '/discovery', label: 'Discovery Dashboard', icon: '🧭', end: true },
  { to: '/discovery/projects', label: 'Discovery Projects', icon: '🗂️' },
]

interface SidebarProps {
  open: boolean
  onNavigate?: () => void
}

export function Sidebar({ open, onNavigate }: SidebarProps) {
  return (
    <aside
      className={`fixed inset-y-0 left-0 z-30 w-64 transform overflow-y-auto border-r border-slate-200 bg-white transition-transform duration-200 ease-in-out md:static md:translate-x-0 ${
        open ? 'translate-x-0' : '-translate-x-full'
      }`}
    >
      <div className="flex h-16 items-center gap-2 border-b border-slate-200 px-6">
        <span className="text-xl">🏨</span>
        <span className="text-lg font-semibold text-slate-900">Hospital AI Copilot</span>
      </div>
      <nav className="flex flex-col gap-4 p-3">
        <NavGroup label="Copilot" items={COPILOT_ITEMS} onNavigate={onNavigate} />
        <NavGroup label="Records" items={RECORDS_ITEMS} onNavigate={onNavigate} />
        <NavGroup label="Hospital Discovery" items={DISCOVERY_ITEMS} onNavigate={onNavigate} />
      </nav>
    </aside>
  )
}

interface NavItem {
  to: string
  label: string
  icon: string
  end?: boolean
}

function NavGroup({ label, items, onNavigate }: { label: string; items: NavItem[]; onNavigate?: () => void }) {
  return (
    <div>
      <p className="px-3 pb-1 text-xs font-semibold uppercase tracking-wide text-slate-400">{label}</p>
      <div className="flex flex-col gap-1">
        {items.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.end}
            onClick={onNavigate}
            className={({ isActive }) =>
              `flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors ${
                isActive ? 'bg-slate-900 text-white' : 'text-slate-600 hover:bg-slate-100'
              }`
            }
          >
            <span aria-hidden="true">{item.icon}</span>
            {item.label}
          </NavLink>
        ))}
      </div>
    </div>
  )
}
