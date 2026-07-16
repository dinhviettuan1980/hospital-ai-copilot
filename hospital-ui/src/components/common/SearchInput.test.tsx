import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { SearchInput } from './SearchInput'

describe('SearchInput', () => {
  it('calls onChange with the typed value', async () => {
    const onChange = vi.fn()
    render(<SearchInput value="" onChange={onChange} placeholder="Search patients..." />)

    await userEvent.type(screen.getByPlaceholderText('Search patients...'), 'ada')

    expect(onChange).toHaveBeenCalledTimes(3)
    expect(onChange).toHaveBeenLastCalledWith('a')
  })
})
