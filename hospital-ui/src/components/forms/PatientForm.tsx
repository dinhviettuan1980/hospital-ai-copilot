import { useState, type FormEvent } from 'react'
import type { Gender, Patient, PatientInput } from '../../api/types'
import { FormField, inputClass } from '../common/FormField'

interface PatientFormProps {
  initial?: Patient
  onSubmit: (input: PatientInput) => Promise<void>
  onCancel: () => void
}

const GENDERS: Gender[] = ['MALE', 'FEMALE', 'OTHER']

export function PatientForm({ initial, onSubmit, onCancel }: PatientFormProps) {
  const [firstName, setFirstName] = useState(initial?.firstName ?? '')
  const [lastName, setLastName] = useState(initial?.lastName ?? '')
  const [dateOfBirth, setDateOfBirth] = useState(initial?.dateOfBirth ?? '')
  const [gender, setGender] = useState<Gender>(initial?.gender ?? 'OTHER')
  const [phone, setPhone] = useState(initial?.phone ?? '')
  const [email, setEmail] = useState(initial?.email ?? '')
  const [address, setAddress] = useState(initial?.address ?? '')
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState<string | null>(null)

  const validate = () => {
    const next: Record<string, string> = {}
    if (!firstName.trim()) next.firstName = 'First name is required'
    if (!lastName.trim()) next.lastName = 'Last name is required'
    if (!dateOfBirth) next.dateOfBirth = 'Date of birth is required'
    else if (new Date(dateOfBirth) >= new Date()) next.dateOfBirth = 'Date of birth must be in the past'
    if (email && !/^\S+@\S+\.\S+$/.test(email)) next.email = 'Enter a valid email address'
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
        firstName: firstName.trim(),
        lastName: lastName.trim(),
        dateOfBirth,
        gender,
        phone: phone.trim(),
        email: email.trim(),
        address: address.trim(),
      })
    } catch (err) {
      setSubmitError(err instanceof Error ? err.message : 'Failed to save patient')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      <div className="grid grid-cols-2 gap-4">
        <FormField label="First name" htmlFor="patient-first-name" error={errors.firstName}>
          <input
            id="patient-first-name"
            className={inputClass}
            value={firstName}
            onChange={(e) => setFirstName(e.target.value)}
          />
        </FormField>
        <FormField label="Last name" htmlFor="patient-last-name" error={errors.lastName}>
          <input
            id="patient-last-name"
            className={inputClass}
            value={lastName}
            onChange={(e) => setLastName(e.target.value)}
          />
        </FormField>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <FormField label="Date of birth" htmlFor="patient-dob" error={errors.dateOfBirth}>
          <input
            id="patient-dob"
            type="date"
            className={inputClass}
            value={dateOfBirth}
            onChange={(e) => setDateOfBirth(e.target.value)}
          />
        </FormField>
        <FormField label="Gender" htmlFor="patient-gender">
          <select
            id="patient-gender"
            className={inputClass}
            value={gender}
            onChange={(e) => setGender(e.target.value as Gender)}
          >
            {GENDERS.map((g) => (
              <option key={g} value={g}>
                {g.charAt(0) + g.slice(1).toLowerCase()}
              </option>
            ))}
          </select>
        </FormField>
      </div>

      <FormField label="Phone" htmlFor="patient-phone">
        <input id="patient-phone" className={inputClass} value={phone} onChange={(e) => setPhone(e.target.value)} />
      </FormField>
      <FormField label="Email" htmlFor="patient-email" error={errors.email}>
        <input
          id="patient-email"
          type="email"
          className={inputClass}
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
      </FormField>
      <FormField label="Address" htmlFor="patient-address">
        <input
          id="patient-address"
          className={inputClass}
          value={address}
          onChange={(e) => setAddress(e.target.value)}
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
