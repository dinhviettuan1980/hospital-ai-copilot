interface ChatMessageProps {
  role: 'user' | 'assistant'
  text: string
  data?: Record<string, unknown> | null
}

export function ChatMessage({ role, text, data }: ChatMessageProps) {
  const isUser = role === 'user'
  return (
    <div className={`flex ${isUser ? 'justify-end' : 'justify-start'}`}>
      <div
        className={`max-w-[80%] rounded-2xl px-4 py-3 text-sm shadow-sm ${
          isUser ? 'bg-slate-900 text-white' : 'border border-slate-200 bg-white text-slate-800'
        }`}
      >
        <p className="whitespace-pre-wrap">{text}</p>
        {data && (
          <details className="mt-2 text-xs opacity-80">
            <summary className="cursor-pointer select-none">View data</summary>
            <pre className="mt-1 overflow-x-auto rounded bg-slate-100 p-2 text-slate-700">
              {JSON.stringify(data, null, 2)}
            </pre>
          </details>
        )}
      </div>
    </div>
  )
}
