import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it } from 'vitest'
import { Sidebar } from './Sidebar'

describe('Sidebar', () => {
  it('renders a navigation link for every page', () => {
    render(
      <MemoryRouter>
        <Sidebar open />
      </MemoryRouter>,
    )

    expect(screen.getByRole('link', { name: /^Dashboard$/ })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /^Departments$/ })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /^Patients$/ })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /^Visits$/ })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /^Discovery Dashboard$/ })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /^Discovery Projects$/ })).toBeInTheDocument()
  })

  it('marks the current route as active', () => {
    render(
      <MemoryRouter initialEntries={['/departments']}>
        <Sidebar open />
      </MemoryRouter>,
    )

    expect(screen.getByRole('link', { name: /^Departments$/ })).toHaveClass('bg-slate-900')
  })
})
