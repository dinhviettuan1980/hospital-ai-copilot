import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { Pagination } from './Pagination'

describe('Pagination', () => {
  it('renders nothing when there are no elements', () => {
    const { container } = render(
      <Pagination page={0} totalPages={0} totalElements={0} onPageChange={vi.fn()} />,
    )

    expect(container).toBeEmptyDOMElement()
  })

  it('shows the current page and total count', () => {
    render(<Pagination page={1} totalPages={5} totalElements={45} onPageChange={vi.fn()} />)

    expect(screen.getByText(/Page 2 of 5/)).toBeInTheDocument()
    expect(screen.getByText(/45 total/)).toBeInTheDocument()
  })

  it('disables Previous on the first page and Next on the last page', () => {
    render(<Pagination page={0} totalPages={1} totalElements={3} onPageChange={vi.fn()} />)

    expect(screen.getByRole('button', { name: 'Previous' })).toBeDisabled()
    expect(screen.getByRole('button', { name: 'Next' })).toBeDisabled()
  })

  it('calls onPageChange with the next page index', async () => {
    const onPageChange = vi.fn()
    render(<Pagination page={0} totalPages={3} totalElements={30} onPageChange={onPageChange} />)

    await userEvent.click(screen.getByRole('button', { name: 'Next' }))

    expect(onPageChange).toHaveBeenCalledWith(1)
  })
})
