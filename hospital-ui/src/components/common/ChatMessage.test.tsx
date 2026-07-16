import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { ChatMessage } from './ChatMessage'

describe('ChatMessage', () => {
  it('renders the message text', () => {
    render(<ChatMessage role="assistant" text="There are 3 ICU beds available." />)

    expect(screen.getByText('There are 3 ICU beds available.')).toBeInTheDocument()
  })

  it('does not render a data disclosure when no data is provided', () => {
    render(<ChatMessage role="user" text="How many ICU beds are available?" />)

    expect(screen.queryByText('View data')).not.toBeInTheDocument()
  })

  it('renders a collapsible data section when data is provided', () => {
    render(<ChatMessage role="assistant" text="Answer" data={{ available: 3, total: 15 }} />)

    expect(screen.getByText('View data')).toBeInTheDocument()
    expect(screen.getByText(/"available": 3/)).toBeInTheDocument()
  })
})
