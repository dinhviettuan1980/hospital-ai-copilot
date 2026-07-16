import { useState } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import { Sidebar } from './Sidebar'
import { TopNav } from './TopNav'

const TITLES: Record<string, string> = {
  '/': 'Executive Dashboard',
  '/command-center': 'Command Center',
  '/ai-director': 'AI Director',
  '/knowledge-center': 'Knowledge Center',
  '/departments': 'Departments',
  '/patients': 'Patients',
  '/visits': 'Visits',
  '/discovery': 'Discovery Dashboard',
  '/discovery/projects': 'Discovery Projects',
}

export function Layout() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const location = useLocation()
  const title = TITLES[location.pathname] ?? (location.pathname.startsWith('/discovery/projects/')
    ? 'Discovery Survey'
    : 'Hospital AI Copilot')

  return (
    <div className="min-h-screen bg-slate-100 md:flex">
      <Sidebar open={sidebarOpen} onNavigate={() => setSidebarOpen(false)} />

      {sidebarOpen && (
        <button
          type="button"
          aria-label="Close navigation overlay"
          onClick={() => setSidebarOpen(false)}
          className="fixed inset-0 z-20 bg-slate-900/30 md:hidden"
        />
      )}

      <div className="flex min-h-screen flex-1 flex-col">
        <TopNav onToggleSidebar={() => setSidebarOpen((open) => !open)} title={title} />
        <main className="flex-1 p-4 sm:p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
