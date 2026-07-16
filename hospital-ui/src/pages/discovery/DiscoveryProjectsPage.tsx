import { useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { discoveryProjectsApi } from '../../api/discoveryProjects'
import type { DiscoveryProject, DiscoveryProjectInput, DiscoverySurveyExport } from '../../api/discoveryTypes'
import { useResourceList } from '../../hooks/useResourceList'
import { LoadingState } from '../../components/common/LoadingState'
import { ErrorState } from '../../components/common/ErrorState'
import { SearchInput } from '../../components/common/SearchInput'
import { Pagination } from '../../components/common/Pagination'
import { SortableHeader } from '../../components/common/SortableHeader'
import { Modal } from '../../components/common/Modal'
import { ConfirmDialog } from '../../components/common/ConfirmDialog'
import { ProgressBar } from '../../components/common/ProgressBar'
import { DiscoveryProjectForm } from '../../components/discovery/DiscoveryProjectForm'

const STATUS_STYLES: Record<string, string> = {
  DRAFT: 'bg-slate-200 text-slate-600',
  IN_PROGRESS: 'bg-blue-100 text-blue-700',
  COMPLETED: 'bg-green-100 text-green-700',
}

export function DiscoveryProjectsPage() {
  const navigate = useNavigate()
  const { state, data, loading, error, setQuery, setPage, setSort, refresh } =
    useResourceList<DiscoveryProject>(discoveryProjectsApi.list)

  const [editing, setEditing] = useState<DiscoveryProject | 'new' | null>(null)
  const [deleting, setDeleting] = useState<DiscoveryProject | null>(null)
  const [deleteBusy, setDeleteBusy] = useState(false)
  const [deleteError, setDeleteError] = useState<string | null>(null)

  const handleSave = async (input: DiscoveryProjectInput) => {
    if (editing === 'new') {
      await discoveryProjectsApi.create(input)
    } else if (editing) {
      await discoveryProjectsApi.update(editing.id, input)
    }
    setEditing(null)
    refresh()
  }

  const handleDelete = async () => {
    if (!deleting) return
    setDeleteBusy(true)
    setDeleteError(null)
    try {
      await discoveryProjectsApi.remove(deleting.id)
      setDeleting(null)
      refresh()
    } catch (err) {
      setDeleteError(err instanceof Error ? err.message : 'Failed to delete discovery project')
    } finally {
      setDeleteBusy(false)
    }
  }

  const importInputRef = useRef<HTMLInputElement>(null)
  const [importing, setImporting] = useState(false)
  const [importError, setImportError] = useState<string | null>(null)

  const handleImportFile = async (file: File | undefined) => {
    if (!file) return
    setImporting(true)
    setImportError(null)
    try {
      const text = await file.text()
      const survey = JSON.parse(text) as DiscoverySurveyExport
      const created = await discoveryProjectsApi.import(survey)
      refresh()
      navigate(`/discovery/projects/${created.id}`)
    } catch (err) {
      setImportError(err instanceof Error ? err.message : 'Failed to import survey — is this a valid export file?')
    } finally {
      setImporting(false)
      if (importInputRef.current) importInputRef.current.value = ''
    }
  }

  return (
    <div>
      <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <SearchInput value={state.q} onChange={setQuery} placeholder="Search projects or hospitals..." />
        <div className="flex gap-2">
          <label className="cursor-pointer rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50">
            {importing ? 'Importing...' : '⬆ Import JSON'}
            <input
              ref={importInputRef}
              type="file"
              accept="application/json"
              className="hidden"
              disabled={importing}
              onChange={(e) => handleImportFile(e.target.files?.[0])}
            />
          </label>
          <button
            type="button"
            onClick={() => setEditing('new')}
            className="rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-700"
          >
            + New Discovery Project
          </button>
        </div>
      </div>

      <div className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
        {loading && <LoadingState />}
        {!loading && error && <ErrorState message={error} onRetry={refresh} />}
        {!loading && !error && data && (
          <>
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-slate-200">
                <thead className="bg-slate-50">
                  <tr>
                    <SortableHeader
                      label="Project"
                      field="projectName"
                      activeField={state.sortBy}
                      direction={state.sortDir}
                      onSort={setSort}
                    />
                    <SortableHeader
                      label="Hospital"
                      field="hospitalName"
                      activeField={state.sortBy}
                      direction={state.sortDir}
                      onSort={setSort}
                    />
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                      Progress
                    </th>
                    <SortableHeader
                      label="Status"
                      field="status"
                      activeField={state.sortBy}
                      direction={state.sortDir}
                      onSort={setSort}
                    />
                    <th className="px-4 py-3" />
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {data.content.length === 0 && (
                    <tr>
                      <td colSpan={5} className="px-4 py-8 text-center text-sm text-slate-500">
                        No discovery projects found.
                      </td>
                    </tr>
                  )}
                  {data.content.map((project) => (
                    <tr key={project.id} className="hover:bg-slate-50">
                      <td className="px-4 py-3 text-sm font-medium text-slate-900">
                        <button
                          type="button"
                          onClick={() => navigate(`/discovery/projects/${project.id}`)}
                          className="text-left hover:underline"
                        >
                          {project.projectName}
                        </button>
                      </td>
                      <td className="px-4 py-3 text-sm text-slate-600">{project.hospitalName}</td>
                      <td className="px-4 py-3 text-sm">
                        <div className="w-32">
                          <ProgressBar percent={project.progressPercent} />
                        </div>
                      </td>
                      <td className="px-4 py-3 text-sm">
                        <span
                          className={`rounded-full px-2 py-0.5 text-xs font-medium ${STATUS_STYLES[project.status]}`}
                        >
                          {project.status.replace('_', ' ')}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-right text-sm">
                        <button
                          type="button"
                          onClick={() => navigate(`/discovery/projects/${project.id}`)}
                          className="mr-3 font-medium text-slate-700 hover:text-slate-900"
                        >
                          Open Survey
                        </button>
                        <button
                          type="button"
                          onClick={() => setEditing(project)}
                          className="mr-3 font-medium text-slate-700 hover:text-slate-900"
                        >
                          Edit
                        </button>
                        <button
                          type="button"
                          onClick={() => setDeleting(project)}
                          className="font-medium text-red-600 hover:text-red-800"
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <Pagination
              page={data.page}
              totalPages={data.totalPages}
              totalElements={data.totalElements}
              onPageChange={setPage}
            />
          </>
        )}
      </div>

      {editing && (
        <Modal
          title={editing === 'new' ? 'New Discovery Project' : 'Edit Discovery Project'}
          onClose={() => setEditing(null)}
        >
          <DiscoveryProjectForm
            initial={editing === 'new' ? undefined : editing}
            onSubmit={handleSave}
            onCancel={() => setEditing(null)}
          />
        </Modal>
      )}

      {deleting && (
        <ConfirmDialog
          title="Delete discovery project"
          message={`Are you sure you want to delete "${deleting.projectName}"? All answers and attachments will be deleted too. This cannot be undone.`}
          onConfirm={handleDelete}
          onCancel={() => setDeleting(null)}
          busy={deleteBusy}
        />
      )}
      {deleteError && <p className="mt-2 text-sm text-red-600">{deleteError}</p>}
      {importError && <p className="mt-2 text-sm text-red-600">{importError}</p>}
    </div>
  )
}
