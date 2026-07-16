import { useState } from 'react'
import type { DiscoveryQuestionWithAnswer, DiscoveryRiskLevel } from '../../api/discoveryTypes'
import { useAutoSaveAnswer } from '../../hooks/useAutoSaveAnswer'
import { AnswerInput } from './AnswerInput'
import { AttachmentPanel } from './AttachmentPanel'
import { inputClass } from '../common/FormField'

interface QuestionCardProps {
  projectId: string
  question: DiscoveryQuestionWithAnswer
  onAnswered: () => void
}

const RISK_OPTIONS: (DiscoveryRiskLevel | '')[] = ['', 'LOW', 'MEDIUM', 'HIGH']
const RISK_LABELS: Record<string, string> = { '': 'No risk flag', LOW: 'Low', MEDIUM: 'Medium', HIGH: 'High' }
const RISK_STYLES: Record<string, string> = {
  LOW: 'text-green-700',
  MEDIUM: 'text-amber-700',
  HIGH: 'text-red-700',
}

const STATUS_LABEL: Record<string, string> = {
  idle: '',
  saving: 'Saving...',
  saved: 'Saved',
  error: 'Failed to save',
}

export function QuestionCard({ projectId, question, onAnswered }: QuestionCardProps) {
  const { value, setValue, comment, setComment, riskLevel, setRiskLevel, status } = useAutoSaveAnswer(
    projectId,
    question,
    onAnswered,
  )
  const [attachments, setAttachments] = useState(question.attachments)
  const [showComment, setShowComment] = useState(Boolean(question.answer?.comment))

  return (
    <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <div className="mb-2 flex items-start justify-between gap-3">
        <div>
          <p className="text-xs font-medium text-slate-400">{question.code}</p>
          <h3 className="font-medium text-slate-900">{question.title}</h3>
          {question.description && <p className="mt-0.5 text-sm text-slate-500">{question.description}</p>}
        </div>
        {status !== 'idle' && (
          <span className={`shrink-0 text-xs ${status === 'error' ? 'text-red-600' : 'text-slate-400'}`}>
            {STATUS_LABEL[status]}
          </span>
        )}
      </div>

      <AnswerInput answerType={question.answerType} options={question.options} value={value} onChange={setValue} />

      {question.answerType === 'FILE_ATTACHMENT' && (
        <AttachmentPanel
          projectId={projectId}
          questionId={question.id}
          attachments={attachments}
          onChanged={setAttachments}
        />
      )}

      <div className="mt-3 flex flex-wrap items-center gap-4 border-t border-slate-100 pt-3">
        <button
          type="button"
          onClick={() => setShowComment((v) => !v)}
          className="text-xs font-medium text-slate-500 hover:text-slate-800"
        >
          {showComment ? 'Hide comment' : '+ Comment'}
        </button>

        <div className="flex items-center gap-2">
          <label htmlFor={`risk-${question.id}`} className="text-xs font-medium text-slate-500">
            Risk:
          </label>
          <select
            id={`risk-${question.id}`}
            className={`${RISK_STYLES[riskLevel] ?? ''} rounded-md border border-slate-300 px-2 py-1 text-xs`}
            value={riskLevel}
            onChange={(e) => setRiskLevel(e.target.value as DiscoveryRiskLevel | '')}
          >
            {RISK_OPTIONS.map((option) => (
              <option key={option} value={option}>
                {RISK_LABELS[option]}
              </option>
            ))}
          </select>
        </div>
      </div>

      {showComment && (
        <textarea
          className={`${inputClass} mt-2`}
          rows={2}
          placeholder="Add a comment..."
          value={comment}
          onChange={(e) => setComment(e.target.value)}
        />
      )}
    </div>
  )
}
