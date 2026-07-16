import { useEffect, useRef, useState, type FormEvent } from 'react'
import { aiDirectorApi } from '../api/aiDirector'
import { ApiError } from '../api/client'
import { ChatMessage } from '../components/common/ChatMessage'

interface Message {
  role: 'user' | 'assistant'
  text: string
  data?: Record<string, unknown> | null
}

const EXAMPLE_QUESTIONS = [
  'How many ICU beds are available?',
  'How many patients visited today?',
  'Which department has the highest number of visits?',
  'Which department has the lowest workload?',
  "What is today's revenue?",
  "Show today's hospital summary",
]

const WELCOME_MESSAGE: Message = {
  role: 'assistant',
  text: "Hello, I'm the AI Director. Ask me about today's operations — patients, ICU capacity, department workload, or revenue. (Demo engine: answers are computed from live data with business rules, not a language model — yet.)",
}

export function AiDirectorPage() {
  const [messages, setMessages] = useState<Message[]>([WELCOME_MESSAGE])
  const [input, setInput] = useState('')
  const [sending, setSending] = useState(false)
  const bottomRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const ask = async (question: string) => {
    const trimmed = question.trim()
    if (!trimmed || sending) return

    setMessages((prev) => [...prev, { role: 'user', text: trimmed }])
    setInput('')
    setSending(true)
    try {
      const response = await aiDirectorApi.ask(trimmed)
      setMessages((prev) => [...prev, { role: 'assistant', text: response.answer, data: response.data }])
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Something went wrong answering that question.'
      setMessages((prev) => [...prev, { role: 'assistant', text: message }])
    } finally {
      setSending(false)
    }
  }

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    ask(input)
  }

  return (
    <div className="flex h-[calc(100vh-8rem)] flex-col rounded-lg border border-slate-200 bg-white shadow-sm">
      <div className="border-b border-slate-200 px-5 py-4">
        <h2 className="font-semibold text-slate-900">AI Director</h2>
        <p className="text-xs text-slate-400">Demo engine — rule-based answers over live hospital data.</p>
      </div>

      <div className="flex-1 space-y-3 overflow-y-auto px-5 py-4">
        {messages.map((message, index) => (
          <ChatMessage key={index} role={message.role} text={message.text} data={message.data} />
        ))}
        {sending && <ChatMessage role="assistant" text="Thinking..." />}
        <div ref={bottomRef} />
      </div>

      <div className="border-t border-slate-200 px-5 py-3">
        <div className="mb-3 flex flex-wrap gap-2">
          {EXAMPLE_QUESTIONS.map((question) => (
            <button
              key={question}
              type="button"
              onClick={() => ask(question)}
              disabled={sending}
              className="rounded-full border border-slate-300 px-3 py-1 text-xs text-slate-600 hover:bg-slate-50 disabled:opacity-50"
            >
              {question}
            </button>
          ))}
        </div>
        <form onSubmit={handleSubmit} className="flex gap-2">
          <input
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Ask the AI Director a question..."
            aria-label="Ask the AI Director"
            className="flex-1 rounded-md border border-slate-300 px-3 py-2 text-sm shadow-sm focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500"
          />
          <button
            type="submit"
            disabled={sending || !input.trim()}
            className="rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-700 disabled:opacity-50"
          >
            Send
          </button>
        </form>
      </div>
    </div>
  )
}
