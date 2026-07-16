import { useEffect, useRef, useState } from 'react'
import { discoverySurveyApi } from '../api/discoverySurvey'
import type { DiscoveryQuestionWithAnswer, DiscoveryRiskLevel } from '../api/discoveryTypes'

export type SaveStatus = 'idle' | 'saving' | 'saved' | 'error'

const AUTO_SAVE_DELAY_MS = 600

/**
 * Owns one question's answer form state and debounce-saves it via PUT
 * whenever value/comment/riskLevel change — the auto-save the spec asks
 * for, scoped to a single question so each one saves independently and a
 * slow network on one field never blocks the rest of the page.
 */
export function useAutoSaveAnswer(
  projectId: string,
  question: DiscoveryQuestionWithAnswer,
  onSaved?: () => void,
) {
  const [value, setValue] = useState(question.answer?.answerValue ?? '')
  const [comment, setComment] = useState(question.answer?.comment ?? '')
  const [riskLevel, setRiskLevel] = useState<DiscoveryRiskLevel | ''>(question.answer?.riskLevel ?? '')
  const [status, setStatus] = useState<SaveStatus>('idle')
  const isFirstRender = useRef(true)

  useEffect(() => {
    if (isFirstRender.current) {
      isFirstRender.current = false
      return
    }

    setStatus('idle')
    const timeout = window.setTimeout(async () => {
      setStatus('saving')
      try {
        await discoverySurveyApi.saveAnswer(projectId, question.id, {
          answerValue: value || null,
          comment: comment || null,
          riskLevel: riskLevel || null,
        })
        setStatus('saved')
        onSaved?.()
      } catch {
        setStatus('error')
      }
    }, AUTO_SAVE_DELAY_MS)

    return () => window.clearTimeout(timeout)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [value, comment, riskLevel])

  return { value, setValue, comment, setComment, riskLevel, setRiskLevel, status }
}
