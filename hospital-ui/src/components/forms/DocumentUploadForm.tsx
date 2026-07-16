import { useState, type FormEvent } from 'react'
import type { DocumentCategory } from '../../api/types'
import { FormField, inputClass } from '../common/FormField'

interface DocumentUploadFormProps {
  categories: DocumentCategory[]
  onSubmit: (title: string, categoryId: string, file: File) => Promise<void>
  onCancel: () => void
}

export function DocumentUploadForm({ categories, onSubmit, onCancel }: DocumentUploadFormProps) {
  const [title, setTitle] = useState('')
  const [categoryId, setCategoryId] = useState(categories[0]?.id ?? '')
  const [file, setFile] = useState<File | null>(null)
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState<string | null>(null)

  const validate = () => {
    const next: Record<string, string> = {}
    if (!title.trim()) next.title = 'Title is required'
    if (!categoryId) next.categoryId = 'Category is required'
    if (!file) {
      next.file = 'A PDF or DOCX file is required'
    } else if (!/\.(pdf|docx)$/i.test(file.name)) {
      next.file = 'Only .pdf and .docx files are supported'
    }
    setErrors(next)
    return Object.keys(next).length === 0
  }

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    if (!validate() || !file) return

    setSubmitting(true)
    setSubmitError(null)
    try {
      await onSubmit(title.trim(), categoryId, file)
    } catch (err) {
      setSubmitError(err instanceof Error ? err.message : 'Failed to upload document')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      <FormField label="Title" htmlFor="document-title" error={errors.title}>
        <input id="document-title" className={inputClass} value={title} onChange={(e) => setTitle(e.target.value)} />
      </FormField>

      <FormField label="Category" htmlFor="document-category" error={errors.categoryId}>
        <select
          id="document-category"
          className={inputClass}
          value={categoryId}
          onChange={(e) => setCategoryId(e.target.value)}
        >
          <option value="" disabled>
            Select a category
          </option>
          {categories.map((category) => (
            <option key={category.id} value={category.id}>
              {category.name}
            </option>
          ))}
        </select>
      </FormField>

      <FormField label="File (PDF or DOCX)" htmlFor="document-file" error={errors.file}>
        <input
          id="document-file"
          type="file"
          accept=".pdf,.docx"
          className={inputClass}
          onChange={(e) => setFile(e.target.files?.[0] ?? null)}
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
          {submitting ? 'Uploading...' : 'Upload'}
        </button>
      </div>
    </form>
  )
}
