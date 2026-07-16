import { Route, Routes } from 'react-router-dom'
import { Layout } from './components/layout/Layout'
import { DashboardPage } from './pages/DashboardPage'
import { CommandCenterPage } from './pages/CommandCenterPage'
import { AiDirectorPage } from './pages/AiDirectorPage'
import { KnowledgeCenterPage } from './pages/KnowledgeCenterPage'
import { DepartmentsPage } from './pages/DepartmentsPage'
import { PatientsPage } from './pages/PatientsPage'
import { VisitsPage } from './pages/VisitsPage'
import { DiscoveryDashboardPage } from './pages/discovery/DiscoveryDashboardPage'
import { DiscoveryProjectsPage } from './pages/discovery/DiscoveryProjectsPage'
import { DiscoveryProjectWorkspacePage } from './pages/discovery/DiscoveryProjectWorkspacePage'

function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<DashboardPage />} />
        <Route path="command-center" element={<CommandCenterPage />} />
        <Route path="ai-director" element={<AiDirectorPage />} />
        <Route path="knowledge-center" element={<KnowledgeCenterPage />} />
        <Route path="departments" element={<DepartmentsPage />} />
        <Route path="patients" element={<PatientsPage />} />
        <Route path="visits" element={<VisitsPage />} />
        <Route path="discovery" element={<DiscoveryDashboardPage />} />
        <Route path="discovery/projects" element={<DiscoveryProjectsPage />} />
        <Route path="discovery/projects/:projectId" element={<DiscoveryProjectWorkspacePage />} />
      </Route>
    </Routes>
  )
}

export default App
