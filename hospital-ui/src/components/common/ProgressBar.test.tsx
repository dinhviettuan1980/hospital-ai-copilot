import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { ProgressBar } from './ProgressBar'

describe('ProgressBar', () => {
  it('renders the label and rounded percent', () => {
    render(<ProgressBar percent={42.6} label="Overall progress" />)

    expect(screen.getByText('Overall progress')).toBeInTheDocument()
    expect(screen.getByText('43%')).toBeInTheDocument()
  })

  it('clamps values above 100 and below 0', () => {
    render(<ProgressBar percent={150} />)
    expect(screen.getByRole('progressbar')).toHaveAttribute('aria-valuenow', '100')

    render(<ProgressBar percent={-20} />)
    expect(screen.getAllByRole('progressbar')[1]).toHaveAttribute('aria-valuenow', '0')
  })
})
