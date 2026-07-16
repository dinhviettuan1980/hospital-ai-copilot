import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { DepartmentForm } from './DepartmentForm'

describe('DepartmentForm', () => {
  it('shows validation errors and does not submit when required fields are empty', async () => {
    const onSubmit = vi.fn()
    render(<DepartmentForm onSubmit={onSubmit} onCancel={vi.fn()} />)

    await userEvent.click(screen.getByRole('button', { name: 'Save' }))

    expect(await screen.findByText('Name is required')).toBeInTheDocument()
    expect(screen.getByText('Code is required')).toBeInTheDocument()
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('submits the trimmed, upper-cased code when the form is valid', async () => {
    const onSubmit = vi.fn().mockResolvedValue(undefined)
    render(<DepartmentForm onSubmit={onSubmit} onCancel={vi.fn()} />)

    await userEvent.type(screen.getByLabelText('Name'), 'Cardiology')
    await userEvent.type(screen.getByLabelText('Code'), 'card')
    await userEvent.click(screen.getByRole('button', { name: 'Save' }))

    expect(onSubmit).toHaveBeenCalledWith({ name: 'Cardiology', code: 'CARD', description: '' })
  })

  it('calls onCancel when the cancel button is clicked', async () => {
    const onCancel = vi.fn()
    render(<DepartmentForm onSubmit={vi.fn()} onCancel={onCancel} />)

    await userEvent.click(screen.getByRole('button', { name: 'Cancel' }))

    expect(onCancel).toHaveBeenCalledOnce()
  })
})
