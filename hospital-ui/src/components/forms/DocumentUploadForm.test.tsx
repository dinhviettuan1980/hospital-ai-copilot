import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { DocumentUploadForm } from './DocumentUploadForm'

const categories = [
  { id: 'cat-1', name: 'Policy' },
  { id: 'cat-2', name: 'SOP' },
]

describe('DocumentUploadForm', () => {
  it('shows validation errors when submitted empty', async () => {
    const onSubmit = vi.fn()
    render(<DocumentUploadForm categories={categories} onSubmit={onSubmit} onCancel={vi.fn()} />)

    await userEvent.click(screen.getByRole('button', { name: 'Upload' }))

    expect(await screen.findByText('Title is required')).toBeInTheDocument()
    expect(screen.getByText('A PDF or DOCX file is required')).toBeInTheDocument()
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('submits title, category, and file when valid', async () => {
    const onSubmit = vi.fn().mockResolvedValue(undefined)
    render(<DocumentUploadForm categories={categories} onSubmit={onSubmit} onCancel={vi.fn()} />)

    await userEvent.type(screen.getByLabelText('Title'), 'ICU Policy')
    await userEvent.selectOptions(screen.getByLabelText('Category'), 'cat-2')
    const file = new File(['content'], 'policy.pdf', { type: 'application/pdf' })
    await userEvent.upload(screen.getByLabelText('File (PDF or DOCX)'), file)
    await userEvent.click(screen.getByRole('button', { name: 'Upload' }))

    expect(onSubmit).toHaveBeenCalledWith('ICU Policy', 'cat-2', file)
  })
})
