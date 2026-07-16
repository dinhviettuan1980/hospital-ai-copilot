import { useState } from 'react'
import { departmentsApi } from '../api/departments'
import type { Department, DepartmentInput } from '../api/types'
import { useResourceList } from '../hooks/useResourceList'
import { LoadingState } from '../components/common/LoadingState'
import { ErrorState } from '../components/common/ErrorState'
import { SearchInput } from '../components/common/SearchInput'
import { Pagination } from '../components/common/Pagination'
import { SortableHeader } from '../components/common/SortableHeader'
import { Modal } from '../components/common/Modal'
import { ConfirmDialog } from '../components/common/ConfirmDialog'
import { DepartmentForm } from '../components/forms/DepartmentForm'

export function DepartmentsPage() {
  const { state, data, loading, error, setQuery, setPage, setSort, refresh } =
    useResourceList<Department>(departmentsApi.list)

  const [editing, setEditing] = useState<Department | 'new' | null>(null)
  const [deleting, setDeleting] = useState<Department | null>(null)
  const [deleteBusy, setDeleteBusy] = useState(false)
  const [deleteError, setDeleteError] = useState<string | null>(null)

  const handleSave = async (input: DepartmentInput) => {
    if (editing === 'new') {
      await departmentsApi.create(input)
    } else if (editing) {
      await departmentsApi.update(editing.id, input)
    }
    setEditing(null)
    refresh()
  }

  const handleDelete = async () => {
    if (!deleting) return
    setDeleteBusy(true)
    setDeleteError(null)
    try {
      await departmentsApi.remove(deleting.id)
      setDeleting(null)
      refresh()
    } catch (err) {
      setDeleteError(err instanceof Error ? err.message : 'Failed to delete department')
    } finally {
      setDeleteBusy(false)
    }
  }

  return (
    <div>
      <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <SearchInput value={state.q} onChange={setQuery} placeholder="Search departments..." />
        <button
          type="button"
          onClick={() => setEditing('new')}
          className="rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-700"
        >
          + New Department
        </button>
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
                      label="Name"
                      field="name"
                      activeField={state.sortBy}
                      direction={state.sortDir}
                      onSort={setSort}
                    />
                    <SortableHeader
                      label="Code"
                      field="code"
                      activeField={state.sortBy}
                      direction={state.sortDir}
                      onSort={setSort}
                    />
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                      Description
                    </th>
                    <th className="px-4 py-3" />
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {data.content.length === 0 && (
                    <tr>
                      <td colSpan={4} className="px-4 py-8 text-center text-sm text-slate-500">
                        No departments found.
                      </td>
                    </tr>
                  )}
                  {data.content.map((department) => (
                    <tr key={department.id} className="hover:bg-slate-50">
                      <td className="px-4 py-3 text-sm font-medium text-slate-900">{department.name}</td>
                      <td className="px-4 py-3 text-sm text-slate-600">{department.code}</td>
                      <td className="px-4 py-3 text-sm text-slate-500">{department.description}</td>
                      <td className="px-4 py-3 text-right text-sm">
                        <button
                          type="button"
                          onClick={() => setEditing(department)}
                          className="mr-3 font-medium text-slate-700 hover:text-slate-900"
                        >
                          Edit
                        </button>
                        <button
                          type="button"
                          onClick={() => setDeleting(department)}
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
        <Modal title={editing === 'new' ? 'New Department' : 'Edit Department'} onClose={() => setEditing(null)}>
          <DepartmentForm
            initial={editing === 'new' ? undefined : editing}
            onSubmit={handleSave}
            onCancel={() => setEditing(null)}
          />
        </Modal>
      )}

      {deleting && (
        <ConfirmDialog
          title="Delete department"
          message={`Are you sure you want to delete "${deleting.name}"? This cannot be undone.`}
          onConfirm={handleDelete}
          onCancel={() => setDeleting(null)}
          busy={deleteBusy}
        />
      )}
      {deleteError && <p className="mt-2 text-sm text-red-600">{deleteError}</p>}
    </div>
  )
}
