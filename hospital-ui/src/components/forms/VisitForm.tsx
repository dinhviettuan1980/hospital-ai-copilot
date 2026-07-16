import { useState, type FormEvent } from 'react'
import type { Department, Patient, Visit, VisitInput, VisitStatus } from '../../api/types'
import { FormField, inputClass } from '../common/FormField'

interface VisitFormProps {
  initial?: Visit
  departments: Department[]
  patients: Patient[]
  onSubmit: (input: VisitInput) => Promise<void>
  onCancel: () => void
}

const STATUSES: VisitStatus[] = ['SCHEDULED', 'COMPLETED', 'CANCELLED']

function toLocalDateTimeInput(value?: string): string {
  if (!value) return ''
  return value.slice(0, 16)
}

export function VisitForm({ initial, departments, patients, onSubmit, onCancel }: VisitFormProps) {
  const [patientId, setPatientId] = useState(initial?.patient.id ?? patients[0]?.id ?? '')
  const [departmentId, setDepartmentId] = useState(initial?.department.id ?? departments[0]?.id ?? '')
  const [visitDate, setVisitDate] = useState(toLocalDateTimeInput(initial?.visitDate))
  const [reason, setReason] = useState(initial?.reason ?? '')
  const [status, setStatus] = useState<VisitStatus>(initial?.status ?? 'SCHEDULED')
  const [notes, setNotes] = useState(initial?.notes ?? '')
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState<string | null>(null)

  const validate = () => {
    const next: Record<string, string> = {}
    if (!patientId) next.patientId = 'Patient is required'
    if (!departmentId) next.departmentId = 'Department is required'
    if (!visitDate) next.visitDate = 'Visit date/time is required'
    if (!reason.trim()) next.reason = 'Reason is required'
    setErrors(next)
    return Object.keys(next).length === 0
  }

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    if (!validate()) return

    setSubmitting(true)
    setSubmitError(null)
    try {
      await onSubmit({
        patientId,
        departmentId,
        visitDate,
        reason: reason.trim(),
        status,
        notes: notes.trim(),
      })
    } catch (err) {
      setSubmitError(err instanceof Error ? err.message : 'Failed to save visit')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      <FormField label="Patient" htmlFor="visit-patient" error={errors.patientId}>
        <select
          id="visit-patient"
          className={inputClass}
          value={patientId}
          onChange={(e) => setPatientId(e.target.value)}
        >
          <option value="" disabled>
            Select a patient
          </option>
          {patients.map((p) => (
            <option key={p.id} value={p.id}>
              {p.fullName}
            </option>
          ))}
        </select>
      </FormField>

      <FormField label="Department" htmlFor="visit-department" error={errors.departmentId}>
        <select
          id="visit-department"
          className={inputClass}
          value={departmentId}
          onChange={(e) => setDepartmentId(e.target.value)}
        >
          <option value="" disabled>
            Select a department
          </option>
          {departments.map((d) => (
            <option key={d.id} value={d.id}>
              {d.name}
            </option>
          ))}
        </select>
      </FormField>

      <div className="grid grid-cols-2 gap-4">
        <FormField label="Visit date/time" htmlFor="visit-date" error={errors.visitDate}>
          <input
            id="visit-date"
            type="datetime-local"
            className={inputClass}
            value={visitDate}
            onChange={(e) => setVisitDate(e.target.value)}
          />
        </FormField>
        <FormField label="Status" htmlFor="visit-status">
          <select
            id="visit-status"
            className={inputClass}
            value={status}
            onChange={(e) => setStatus(e.target.value as VisitStatus)}
          >
            {STATUSES.map((s) => (
              <option key={s} value={s}>
                {s.charAt(0) + s.slice(1).toLowerCase()}
              </option>
            ))}
          </select>
        </FormField>
      </div>

      <FormField label="Reason" htmlFor="visit-reason" error={errors.reason}>
        <input id="visit-reason" className={inputClass} value={reason} onChange={(e) => setReason(e.target.value)} />
      </FormField>

      <FormField label="Notes" htmlFor="visit-notes">
        <textarea
          id="visit-notes"
          className={inputClass}
          rows={3}
          value={notes}
          onChange={(e) => setNotes(e.target.value)}
        />
      </FormField>

      {submitError && <p className="mb-4 text-sm text-red-600">{submitError}</p>}

      <div className="flex justify-end gap-3">
        <button
          type="button"
          onClick={onCancel}
          className="rounded-md border border-slate-300 px-3 py-1.5 text-sm font-medium text-slate-700 hover:bg-slate-50"
        >
          Cancel
        </button>
        <button
          type="submit"
          disabled={submitting}
          className="rounded-md bg-slate-900 px-3 py-1.5 text-sm font-medium text-white hover:bg-slate-700 disabled:opacity-50"
        >
          {submitting ? 'Saving...' : 'Save'}
        </button>
      </div>
    </form>
  )
}
