import type { DiscoveryAnswerType } from '../../api/discoveryTypes'
import { inputClass } from '../common/FormField'

interface AnswerInputProps {
  answerType: DiscoveryAnswerType
  options: string[]
  value: string
  onChange: (value: string) => void
}

/** Parses the JSON-array-as-string that MULTIPLE_CHOICE answers are stored as. */
function parseMultiValue(value: string): string[] {
  if (!value) return []
  try {
    const parsed = JSON.parse(value)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

export function AnswerInput({ answerType, options, value, onChange }: AnswerInputProps) {
  switch (answerType) {
    case 'TEXT':
      return (
        <textarea
          className={inputClass}
          rows={3}
          value={value}
          onChange={(e) => onChange(e.target.value)}
          placeholder="Type your answer..."
        />
      )

    case 'NUMBER':
      return (
        <input
          type="number"
          className={inputClass}
          value={value}
          onChange={(e) => onChange(e.target.value)}
        />
      )

    case 'DATE':
      return <input type="date" className={inputClass} value={value} onChange={(e) => onChange(e.target.value)} />

    case 'URL':
      return (
        <input
          type="url"
          className={inputClass}
          value={value}
          placeholder="https://..."
          onChange={(e) => onChange(e.target.value)}
        />
      )

    case 'YES_NO':
      return (
        <div className="flex gap-4">
          {['Yes', 'No'].map((option) => (
            <label key={option} className="flex items-center gap-2 text-sm text-slate-700">
              <input
                type="radio"
                name={`yes-no-${option}`}
                checked={value === option}
                onChange={() => onChange(option)}
              />
              {option}
            </label>
          ))}
        </div>
      )

    case 'SINGLE_CHOICE':
      return (
        <select className={inputClass} value={value} onChange={(e) => onChange(e.target.value)}>
          <option value="">Select an option</option>
          {options.map((option) => (
            <option key={option} value={option}>
              {option}
            </option>
          ))}
        </select>
      )

    case 'MULTIPLE_CHOICE': {
      const selected = parseMultiValue(value)
      const toggle = (option: string) => {
        const next = selected.includes(option) ? selected.filter((o) => o !== option) : [...selected, option]
        onChange(JSON.stringify(next))
      }
      return (
        <div className="flex flex-col gap-2">
          {options.map((option) => (
            <label key={option} className="flex items-center gap-2 text-sm text-slate-700">
              <input type="checkbox" checked={selected.includes(option)} onChange={() => toggle(option)} />
              {option}
            </label>
          ))}
        </div>
      )
    }

    case 'RATING':
      return (
        <div className="flex gap-2">
          {[1, 2, 3, 4, 5].map((rating) => (
            <button
              key={rating}
              type="button"
              onClick={() => onChange(String(rating))}
              className={`h-9 w-9 rounded-md border text-sm font-medium ${
                Number(value) >= rating
                  ? 'border-amber-400 bg-amber-400 text-white'
                  : 'border-slate-300 text-slate-500 hover:bg-slate-50'
              }`}
              aria-label={`Rate ${rating}`}
            >
              {rating}
            </button>
          ))}
        </div>
      )

    case 'FILE_ATTACHMENT':
      return <p className="text-sm text-slate-400">Attach the supporting file below.</p>

    default:
      return <input className={inputClass} value={value} onChange={(e) => onChange(e.target.value)} />
  }
}
