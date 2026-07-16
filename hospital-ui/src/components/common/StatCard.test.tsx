import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { StatCard } from './StatCard'

describe('StatCard', () => {
  it('renders the label and value', () => {
    render(<StatCard label="Total Patients" value={42} />)

    expect(screen.getByText('Total Patients')).toBeInTheDocument()
    expect(screen.getByText('42')).toBeInTheDocument()
  })
})
