import { useState, type FormEvent } from 'react'
import type { Department, DepartmentInput } from '../../api/types'
import { FormField, inputClass } from '../common/FormField'

interface DepartmentFormProps {
  initial?: Department
  onSubmit: (input: DepartmentInput) => Promise<void>
  onCancel: () => void
}

export function DepartmentForm({ initial, onSubmit, onCancel }: DepartmentFormProps) {
  const [name, setName] = useState(initial?.name ?? '')
  const [code, setCode] = useState(initial?.code ?? '')
  const [description, setDescription] = useState(initial?.description ?? '')
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState<string | null>(null)

  const validate = () => {
    const next: Record<string, string> = {}
    if (!name.trim()) next.name = 'Name is required'
    if (!code.trim()) next.code = 'Code is required'
    else if (code.trim().length > 10) next.code = 'Code must be 10 characters or fewer'
    setErrors(next)
    return Object.keys(next).length === 0
  }

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    if (!validate()) return

    setSubmitting(true)
    setSubmitError(null)
    try {
      await onSubmit({ name: name.trim(), code: code.trim().toUpperCase(), description: description.trim() })
    } catch (err) {
      setSubmitError(err instanceof Error ? err.message : 'Failed to save department')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      <FormField label="Name" htmlFor="department-name" error={errors.name}>
        <input
          id="department-name"
          className={inputClass}
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
      </FormField>
      <FormField label="Code" htmlFor="department-code" error={errors.code}>
        <input
          id="department-code"
          className={inputClass}
          value={code}
          onChange={(e) => setCode(e.target.value)}
          maxLength={10}
        />
      </FormField>
      <FormField label="Description" htmlFor="department-description">
        <textarea
          id="department-description"
          className={inputClass}
          rows={3}
          value={description}
          onChange={(e) => setDescription(e.target.value)}
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
