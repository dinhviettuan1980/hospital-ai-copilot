import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { AnswerInput } from './AnswerInput'

describe('AnswerInput', () => {
  it('renders a textarea for TEXT questions', async () => {
    const onChange = vi.fn()
    render(<AnswerInput answerType="TEXT" options={[]} value="" onChange={onChange} />)

    await userEvent.type(screen.getByPlaceholderText('Type your answer...'), 'a')

    expect(onChange).toHaveBeenCalledWith('a')
  })

  it('renders Yes/No radio buttons and selects the current value', () => {
    render(<AnswerInput answerType="YES_NO" options={[]} value="Yes" onChange={vi.fn()} />)

    expect(screen.getByRole('radio', { name: 'Yes' })).toBeChecked()
    expect(screen.getByRole('radio', { name: 'No' })).not.toBeChecked()
  })

  it('renders options for SINGLE_CHOICE and reports the selected one', async () => {
    const onChange = vi.fn()
    render(
      <AnswerInput answerType="SINGLE_CHOICE" options={['Cloud', 'On-Premise']} value="" onChange={onChange} />,
    )

    await userEvent.selectOptions(screen.getByRole('combobox'), 'On-Premise')

    expect(onChange).toHaveBeenCalledWith('On-Premise')
  })

  it('toggles options for MULTIPLE_CHOICE and stores them as a JSON array', async () => {
    const onChange = vi.fn()
    render(
      <AnswerInput answerType="MULTIPLE_CHOICE" options={['HL7', 'FHIR']} value="[]" onChange={onChange} />,
    )

    await userEvent.click(screen.getByRole('checkbox', { name: 'FHIR' }))

    expect(onChange).toHaveBeenCalledWith(JSON.stringify(['FHIR']))
  })

  it('renders 1-5 rating buttons and reports the clicked value', async () => {
    const onChange = vi.fn()
    render(<AnswerInput answerType="RATING" options={[]} value="" onChange={onChange} />)

    await userEvent.click(screen.getByRole('button', { name: 'Rate 4' }))

    expect(onChange).toHaveBeenCalledWith('4')
  })
})
