import { useState, type FormEvent } from 'react'
import type { DiscoveryProject, DiscoveryProjectInput, DiscoveryProjectStatus } from '../../api/discoveryTypes'
import { FormField, inputClass } from '../common/FormField'

interface DiscoveryProjectFormProps {
  initial?: DiscoveryProject
  onSubmit: (input: DiscoveryProjectInput) => Promise<void>
  onCancel: () => void
}

const STATUSES: DiscoveryProjectStatus[] = ['DRAFT', 'IN_PROGRESS', 'COMPLETED']

export function DiscoveryProjectForm({ initial, onSubmit, onCancel }: DiscoveryProjectFormProps) {
  const [projectName, setProjectName] = useState(initial?.projectName ?? '')
  const [hospitalName, setHospitalName] = useState(initial?.hospitalName ?? '')
  const [contactPerson, setContactPerson] = useState(initial?.contactPerson ?? '')
  const [contactEmail, setContactEmail] = useState(initial?.contactEmail ?? '')
  const [contactPhone, setContactPhone] = useState(initial?.contactPhone ?? '')
  const [surveyDate, setSurveyDate] = useState(initial?.surveyDate ?? '')
  const [status, setStatus] = useState<DiscoveryProjectStatus>(initial?.status ?? 'DRAFT')
  const [notes, setNotes] = useState(initial?.notes ?? '')
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState<string | null>(null)

  const validate = () => {
    const next: Record<string, string> = {}
    if (!projectName.trim()) next.projectName = 'Project name is required'
    if (!hospitalName.trim()) next.hospitalName = 'Hospital name is required'
    if (contactEmail && !/^\S+@\S+\.\S+$/.test(contactEmail)) next.contactEmail = 'Enter a valid email address'
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
        projectName: projectName.trim(),
        hospitalName: hospitalName.trim(),
        contactPerson: contactPerson.trim(),
        contactEmail: contactEmail.trim(),
        contactPhone: contactPhone.trim(),
        surveyDate,
        status,
        notes: notes.trim(),
      })
    } catch (err) {
      setSubmitError(err instanceof Error ? err.message : 'Failed to save discovery project')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      <FormField label="Project Name" htmlFor="project-name" error={errors.projectName}>
        <input
          id="project-name"
          className={inputClass}
          value={projectName}
          onChange={(e) => setProjectName(e.target.value)}
        />
      </FormField>
      <FormField label="Hospital Name" htmlFor="hospital-name" error={errors.hospitalName}>
        <input
          id="hospital-name"
          className={inputClass}
          value={hospitalName}
          onChange={(e) => setHospitalName(e.target.value)}
        />
      </FormField>

      <div className="grid grid-cols-2 gap-4">
        <FormField label="Contact Person" htmlFor="contact-person">
          <input
            id="contact-person"
            className={inputClass}
            value={contactPerson}
            onChange={(e) => setContactPerson(e.target.value)}
          />
        </FormField>
        <FormField label="Contact Email" htmlFor="contact-email" error={errors.contactEmail}>
          <input
            id="contact-email"
            type="email"
            className={inputClass}
            value={contactEmail}
            onChange={(e) => setContactEmail(e.target.value)}
          />
        </FormField>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <FormField label="Contact Phone" htmlFor="contact-phone">
          <input
            id="contact-phone"
            className={inputClass}
            value={contactPhone}
            onChange={(e) => setContactPhone(e.target.value)}
          />
        </FormField>
        <FormField label="Survey Date" htmlFor="survey-date">
          <input
            id="survey-date"
            type="date"
            className={inputClass}
            value={surveyDate}
            onChange={(e) => setSurveyDate(e.target.value)}
          />
        </FormField>
      </div>

      <FormField label="Status" htmlFor="status">
        <select id="status" className={inputClass} value={status} onChange={(e) => setStatus(e.target.value as DiscoveryProjectStatus)}>
          {STATUSES.map((s) => (
            <option key={s} value={s}>
              {s.replace('_', ' ')}
            </option>
          ))}
        </select>
      </FormField>

      <FormField label="Notes" htmlFor="notes">
        <textarea id="notes" className={inputClass} rows={3} value={notes} onChange={(e) => setNotes(e.target.value)} />
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
