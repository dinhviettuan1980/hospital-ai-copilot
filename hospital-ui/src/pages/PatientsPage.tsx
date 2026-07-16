import { useState } from 'react'
import { patientsApi } from '../api/patients'
import type { Patient, PatientInput } from '../api/types'
import { useResourceList } from '../hooks/useResourceList'
import { LoadingState } from '../components/common/LoadingState'
import { ErrorState } from '../components/common/ErrorState'
import { SearchInput } from '../components/common/SearchInput'
import { Pagination } from '../components/common/Pagination'
import { SortableHeader } from '../components/common/SortableHeader'
import { Modal } from '../components/common/Modal'
import { ConfirmDialog } from '../components/common/ConfirmDialog'
import { PatientForm } from '../components/forms/PatientForm'

export function PatientsPage() {
  const { state, data, loading, error, setQuery, setPage, setSort, refresh } =
    useResourceList<Patient>(patientsApi.list)

  const [editing, setEditing] = useState<Patient | 'new' | null>(null)
  const [deleting, setDeleting] = useState<Patient | null>(null)
  const [deleteBusy, setDeleteBusy] = useState(false)
  const [deleteError, setDeleteError] = useState<string | null>(null)

  const handleSave = async (input: PatientInput) => {
    if (editing === 'new') {
      await patientsApi.create(input)
    } else if (editing) {
      await patientsApi.update(editing.id, input)
    }
    setEditing(null)
    refresh()
  }

  const handleDelete = async () => {
    if (!deleting) return
    setDeleteBusy(true)
    setDeleteError(null)
    try {
      await patientsApi.remove(deleting.id)
      setDeleting(null)
      refresh()
    } catch (err) {
      setDeleteError(err instanceof Error ? err.message : 'Failed to delete patient')
    } finally {
      setDeleteBusy(false)
    }
  }

  return (
    <div>
      <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <SearchInput value={state.q} onChange={setQuery} placeholder="Search patients..." />
        <button
          type="button"
          onClick={() => setEditing('new')}
          className="rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-700"
        >
          + New Patient
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
                      label="Last Name"
                      field="lastName"
                      activeField={state.sortBy}
                      direction={state.sortDir}
                      onSort={setSort}
                    />
                    <SortableHeader
                      label="First Name"
                      field="firstName"
                      activeField={state.sortBy}
                      direction={state.sortDir}
                      onSort={setSort}
                    />
                    <SortableHeader
                      label="Date of Birth"
                      field="dateOfBirth"
                      activeField={state.sortBy}
                      direction={state.sortDir}
                      onSort={setSort}
                    />
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                      Phone
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                      Email
                    </th>
                    <th className="px-4 py-3" />
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {data.content.length === 0 && (
                    <tr>
                      <td colSpan={6} className="px-4 py-8 text-center text-sm text-slate-500">
                        No patients found.
                      </td>
                    </tr>
                  )}
                  {data.content.map((patient) => (
                    <tr key={patient.id} className="hover:bg-slate-50">
                      <td className="px-4 py-3 text-sm font-medium text-slate-900">{patient.lastName}</td>
                      <td className="px-4 py-3 text-sm text-slate-600">{patient.firstName}</td>
                      <td className="px-4 py-3 text-sm text-slate-500">{patient.dateOfBirth}</td>
                      <td className="px-4 py-3 text-sm text-slate-500">{patient.phone}</td>
                      <td className="px-4 py-3 text-sm text-slate-500">{patient.email}</td>
                      <td className="px-4 py-3 text-right text-sm">
                        <button
                          type="button"
                          onClick={() => setEditing(patient)}
                          className="mr-3 font-medium text-slate-700 hover:text-slate-900"
                        >
                          Edit
                        </button>
                        <button
                          type="button"
                          onClick={() => setDeleting(patient)}
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
        <Modal title={editing === 'new' ? 'New Patient' : 'Edit Patient'} onClose={() => setEditing(null)}>
          <PatientForm
            initial={editing === 'new' ? undefined : editing}
            onSubmit={handleSave}
            onCancel={() => setEditing(null)}
          />
        </Modal>
      )}

      {deleting && (
        <ConfirmDialog
          title="Delete patient"
          message={`Are you sure you want to delete "${deleting.fullName}"? This cannot be undone.`}
          onConfirm={handleDelete}
          onCancel={() => setDeleting(null)}
          busy={deleteBusy}
        />
      )}
      {deleteError && <p className="mt-2 text-sm text-red-600">{deleteError}</p>}
    </div>
  )
}
