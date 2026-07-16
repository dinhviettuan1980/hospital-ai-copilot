import { useEffect, useState } from 'react'
import { visitsApi, type VisitListParams } from '../api/visits'
import { departmentsApi } from '../api/departments'
import { patientsApi } from '../api/patients'
import type { Department, Patient, Visit, VisitInput, VisitStatus } from '../api/types'
import { useResourceList } from '../hooks/useResourceList'
import { LoadingState } from '../components/common/LoadingState'
import { ErrorState } from '../components/common/ErrorState'
import { SearchInput } from '../components/common/SearchInput'
import { Pagination } from '../components/common/Pagination'
import { SortableHeader } from '../components/common/SortableHeader'
import { Modal } from '../components/common/Modal'
import { ConfirmDialog } from '../components/common/ConfirmDialog'
import { VisitForm } from '../components/forms/VisitForm'
import { inputClass } from '../components/common/FormField'

const STATUS_OPTIONS: VisitStatus[] = ['SCHEDULED', 'COMPLETED', 'CANCELLED']

export function VisitsPage() {
  const [departments, setDepartments] = useState<Department[]>([])
  const [patients, setPatients] = useState<Patient[]>([])
  const [statusFilter, setStatusFilter] = useState<VisitStatus | ''>('')

  useEffect(() => {
    departmentsApi.list({ size: 100, sortBy: 'name' }).then((res) => setDepartments(res.content))
    patientsApi.list({ size: 200, sortBy: 'lastName' }).then((res) => setPatients(res.content))
  }, [])

  const { state, data, loading, error, setQuery, setPage, setSort, refresh } = useResourceList<
    Visit,
    VisitListParams
  >(visitsApi.list, { status: statusFilter || undefined })

  const [editing, setEditing] = useState<Visit | 'new' | null>(null)
  const [deleting, setDeleting] = useState<Visit | null>(null)
  const [deleteBusy, setDeleteBusy] = useState(false)
  const [deleteError, setDeleteError] = useState<string | null>(null)

  const handleSave = async (input: VisitInput) => {
    if (editing === 'new') {
      await visitsApi.create(input)
    } else if (editing) {
      await visitsApi.update(editing.id, input)
    }
    setEditing(null)
    refresh()
  }

  const handleDelete = async () => {
    if (!deleting) return
    setDeleteBusy(true)
    setDeleteError(null)
    try {
      await visitsApi.remove(deleting.id)
      setDeleting(null)
      refresh()
    } catch (err) {
      setDeleteError(err instanceof Error ? err.message : 'Failed to delete visit')
    } finally {
      setDeleteBusy(false)
    }
  }

  const canCreate = departments.length > 0 && patients.length > 0

  return (
    <div>
      <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
          <SearchInput value={state.q} onChange={setQuery} placeholder="Search visits..." />
          <select
            className={`${inputClass} sm:w-44`}
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value as VisitStatus | '')}
            aria-label="Filter by status"
          >
            <option value="">All statuses</option>
            {STATUS_OPTIONS.map((s) => (
              <option key={s} value={s}>
                {s.charAt(0) + s.slice(1).toLowerCase()}
              </option>
            ))}
          </select>
        </div>
        <button
          type="button"
          onClick={() => setEditing('new')}
          disabled={!canCreate}
          className="rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-700 disabled:opacity-50"
        >
          + New Visit
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
                      label="Date"
                      field="visitDate"
                      activeField={state.sortBy}
                      direction={state.sortDir}
                      onSort={setSort}
                    />
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                      Patient
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                      Department
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                      Reason
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
                      <td colSpan={6} className="px-4 py-8 text-center text-sm text-slate-500">
                        No visits found.
                      </td>
                    </tr>
                  )}
                  {data.content.map((visit) => (
                    <tr key={visit.id} className="hover:bg-slate-50">
                      <td className="px-4 py-3 text-sm text-slate-500">{visit.visitDate.replace('T', ' ')}</td>
                      <td className="px-4 py-3 text-sm font-medium text-slate-900">{visit.patient.fullName}</td>
                      <td className="px-4 py-3 text-sm text-slate-600">{visit.department.name}</td>
                      <td className="px-4 py-3 text-sm text-slate-500">{visit.reason}</td>
                      <td className="px-4 py-3 text-sm">
                        <span
                          className={`rounded-full px-2 py-0.5 text-xs font-medium ${
                            visit.status === 'COMPLETED'
                              ? 'bg-green-100 text-green-700'
                              : visit.status === 'CANCELLED'
                                ? 'bg-slate-200 text-slate-600'
                                : 'bg-blue-100 text-blue-700'
                          }`}
                        >
                          {visit.status}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-right text-sm">
                        <button
                          type="button"
                          onClick={() => setEditing(visit)}
                          className="mr-3 font-medium text-slate-700 hover:text-slate-900"
                        >
                          Edit
                        </button>
                        <button
                          type="button"
                          onClick={() => setDeleting(visit)}
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
        <Modal title={editing === 'new' ? 'New Visit' : 'Edit Visit'} onClose={() => setEditing(null)}>
          <VisitForm
            initial={editing === 'new' ? undefined : editing}
            departments={departments}
            patients={patients}
            onSubmit={handleSave}
            onCancel={() => setEditing(null)}
          />
        </Modal>
      )}

      {deleting && (
        <ConfirmDialog
          title="Delete visit"
          message={`Are you sure you want to delete the visit for "${deleting.patient.fullName}"? This cannot be undone.`}
          onConfirm={handleDelete}
          onCancel={() => setDeleting(null)}
          busy={deleteBusy}
        />
      )}
      {deleteError && <p className="mt-2 text-sm text-red-600">{deleteError}</p>}
    </div>
  )
}
