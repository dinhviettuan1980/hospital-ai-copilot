import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { DiscoveryProjectForm } from './DiscoveryProjectForm'

describe('DiscoveryProjectForm', () => {
  it('shows validation errors when required fields are empty', async () => {
    const onSubmit = vi.fn()
    render(<DiscoveryProjectForm onSubmit={onSubmit} onCancel={vi.fn()} />)

    await userEvent.click(screen.getByRole('button', { name: 'Save' }))

    expect(await screen.findByText('Project name is required')).toBeInTheDocument()
    expect(screen.getByText('Hospital name is required')).toBeInTheDocument()
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('rejects an invalid contact email', async () => {
    const onSubmit = vi.fn()
    render(<DiscoveryProjectForm onSubmit={onSubmit} onCancel={vi.fn()} />)

    await userEvent.type(screen.getByLabelText('Project Name'), 'Discovery Project')
    await userEvent.type(screen.getByLabelText('Hospital Name'), 'Test Hospital')
    await userEvent.type(screen.getByLabelText('Contact Email'), 'not-an-email')
    await userEvent.click(screen.getByRole('button', { name: 'Save' }))

    expect(await screen.findByText('Enter a valid email address')).toBeInTheDocument()
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('submits trimmed values when valid', async () => {
    const onSubmit = vi.fn().mockResolvedValue(undefined)
    render(<DiscoveryProjectForm onSubmit={onSubmit} onCancel={vi.fn()} />)

    await userEvent.type(screen.getByLabelText('Project Name'), '  Discovery Project  ')
    await userEvent.type(screen.getByLabelText('Hospital Name'), 'Test Hospital')
    await userEvent.click(screen.getByRole('button', { name: 'Save' }))

    expect(onSubmit).toHaveBeenCalledWith(
      expect.objectContaining({ projectName: 'Discovery Project', hospitalName: 'Test Hospital', status: 'DRAFT' }),
    )
  })

  it('calls onCancel when the cancel button is clicked', async () => {
    const onCancel = vi.fn()
    render(<DiscoveryProjectForm onSubmit={vi.fn()} onCancel={onCancel} />)

    await userEvent.click(screen.getByRole('button', { name: 'Cancel' }))

    expect(onCancel).toHaveBeenCalledOnce()
  })
})
