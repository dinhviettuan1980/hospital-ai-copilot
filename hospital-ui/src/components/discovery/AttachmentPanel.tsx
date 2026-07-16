import { useRef, useState } from 'react'
import { discoveryAttachmentsApi } from '../../api/discoveryAttachments'
import type { DiscoveryAttachment } from '../../api/discoveryTypes'

interface AttachmentPanelProps {
  projectId: string
  questionId: string
  attachments: DiscoveryAttachment[]
  onChanged: (attachments: DiscoveryAttachment[]) => void
}

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  return `${(bytes / 1024).toFixed(0)} KB`
}

export function AttachmentPanel({ projectId, questionId, attachments, onChanged }: AttachmentPanelProps) {
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const handleFileSelected = async (file: File | undefined) => {
    if (!file) return
    setUploading(true)
    setError(null)
    try {
      const created = await discoveryAttachmentsApi.upload(projectId, file, questionId)
      onChanged([...attachments, created])
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to upload file')
    } finally {
      setUploading(false)
      if (fileInputRef.current) fileInputRef.current.value = ''
    }
  }

  const handleDelete = async (attachmentId: string) => {
    try {
      await discoveryAttachmentsApi.remove(attachmentId)
      onChanged(attachments.filter((a) => a.id !== attachmentId))
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete file')
    }
  }

  return (
    <div className="mt-3 rounded-md border border-dashed border-slate-300 p-3">
      <div className="flex items-center justify-between">
        <p className="text-xs font-medium uppercase tracking-wide text-slate-400">Attachments</p>
        <label className="cursor-pointer text-xs font-medium text-slate-600 hover:text-slate-900">
          {uploading ? 'Uploading...' : '+ Attach file'}
          <input
            ref={fileInputRef}
            type="file"
            accept=".pdf,.docx,.xlsx,.png,.jpg,.jpeg"
            className="hidden"
            disabled={uploading}
            onChange={(e) => handleFileSelected(e.target.files?.[0])}
          />
        </label>
      </div>
      {error && <p className="mt-1 text-xs text-red-600">{error}</p>}
      {attachments.length > 0 && (
        <ul className="mt-2 flex flex-col gap-1">
          {attachments.map((attachment) => (
            <li key={attachment.id} className="flex items-center justify-between text-xs text-slate-600">
              <a
                href={discoveryAttachmentsApi.downloadUrl(attachment.id)}
                className="truncate hover:underline"
              >
                📎 {attachment.fileName} ({formatFileSize(attachment.fileSize)})
              </a>
              <button
                type="button"
                onClick={() => handleDelete(attachment.id)}
                className="ml-2 text-red-500 hover:text-red-700"
              >
                Remove
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
