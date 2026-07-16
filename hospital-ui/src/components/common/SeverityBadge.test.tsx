import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { SeverityBadge } from './SeverityBadge'

describe('SeverityBadge', () => {
  it('renders the human-readable label for each severity', () => {
    const { rerender } = render(<SeverityBadge severity="GREEN" />)
    expect(screen.getByText('Normal')).toBeInTheDocument()

    rerender(<SeverityBadge severity="YELLOW" />)
    expect(screen.getByText('Warning')).toBeInTheDocument()

    rerender(<SeverityBadge severity="RED" />)
    expect(screen.getByText('Critical')).toBeInTheDocument()
  })
})
