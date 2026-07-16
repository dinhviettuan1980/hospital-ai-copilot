import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { ConfirmDialog } from './ConfirmDialog'

describe('ConfirmDialog', () => {
  it('renders the title and message', () => {
    render(
      <ConfirmDialog
        title="Delete department"
        message="Are you sure?"
        onConfirm={vi.fn()}
        onCancel={vi.fn()}
      />,
    )

    expect(screen.getByText('Delete department')).toBeInTheDocument()
    expect(screen.getByText('Are you sure?')).toBeInTheDocument()
  })

  it('calls onConfirm when the confirm button is clicked', async () => {
    const onConfirm = vi.fn()
    render(<ConfirmDialog title="Delete" message="Sure?" onConfirm={onConfirm} onCancel={vi.fn()} />)

    await userEvent.click(screen.getByRole('button', { name: 'Delete' }))

    expect(onConfirm).toHaveBeenCalledOnce()
  })

  it('calls onCancel when the cancel button is clicked', async () => {
    const onCancel = vi.fn()
    render(<ConfirmDialog title="Delete" message="Sure?" onConfirm={vi.fn()} onCancel={onCancel} />)

    await userEvent.click(screen.getByRole('button', { name: 'Cancel' }))

    expect(onCancel).toHaveBeenCalledOnce()
  })

  it('disables the buttons while busy', () => {
    render(<ConfirmDialog title="Delete" message="Sure?" onConfirm={vi.fn()} onCancel={vi.fn()} busy />)

    expect(screen.getByRole('button', { name: 'Deleting...' })).toBeDisabled()
    expect(screen.getByRole('button', { name: 'Cancel' })).toBeDisabled()
  })
})
