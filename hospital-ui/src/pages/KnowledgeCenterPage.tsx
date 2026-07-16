import { useEffect, useState } from 'react'
import { knowledgeApi } from '../api/knowledge'
import type { DocumentCategory, KnowledgeDocument } from '../api/types'
import { useResourceList, type ListState } from '../hooks/useResourceList'
import { LoadingState } from '../components/common/LoadingState'
import { ErrorState } from '../components/common/ErrorState'
import { SearchInput } from '../components/common/SearchInput'
import { Pagination } from '../components/common/Pagination'
import { Modal } from '../components/common/Modal'
import { ConfirmDialog } from '../components/common/ConfirmDialog'
import { DocumentUploadForm } from '../components/forms/DocumentUploadForm'
import { inputClass } from '../components/common/FormField'

const FILE_ICON: Record<string, string> = {
  'application/pdf': '📄',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document': '📝',
}

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  return `${(bytes / 1024).toFixed(0)} KB`
}

export function KnowledgeCenterPage() {
  const [categories, setCategories] = useState<DocumentCategory[]>([])
  const [categoryFilter, setCategoryFilter] = useState('')
  const [newCategoryName, setNewCategoryName] = useState('')
  const [addingCategory, setAddingCategory] = useState(false)

  const loadCategories = () => knowledgeApi.listCategories().then(setCategories)

  useEffect(() => {
    loadCategories()
  }, [])

  const { state, data, loading, error, setQuery, setPage, refresh } = useResourceList<
    KnowledgeDocument,
    ListState & { categoryId?: string }
  >(
    (params) =>
      knowledgeApi.listDocuments({
        title: params.q,
        categoryId: params.categoryId,
        page: params.page,
        size: params.size,
      }),
    { categoryId: categoryFilter || undefined },
  )

  const [uploading, setUploading] = useState(false)
  const [deleting, setDeleting] = useState<KnowledgeDocument | null>(null)
  const [deleteBusy, setDeleteBusy] = useState(false)
  const [deleteError, setDeleteError] = useState<string | null>(null)

  const handleAddCategory = async () => {
    if (!newCategoryName.trim()) return
    setAddingCategory(true)
    try {
      await knowledgeApi.createCategory(newCategoryName.trim())
      setNewCategoryName('')
      await loadCategories()
    } finally {
      setAddingCategory(false)
    }
  }

  const handleUpload = async (title: string, categoryId: string, file: File) => {
    await knowledgeApi.upload(title, categoryId, file)
    setUploading(false)
    refresh()
  }

  const handleDelete = async () => {
    if (!deleting) return
    setDeleteBusy(true)
    setDeleteError(null)
    try {
      await knowledgeApi.remove(deleting.id)
      setDeleting(null)
      refresh()
    } catch (err) {
      setDeleteError(err instanceof Error ? err.message : 'Failed to delete document')
    } finally {
      setDeleteBusy(false)
    }
  }

  return (
    <div>
      <div className="mb-4 flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
          <SearchInput value={state.q} onChange={setQuery} placeholder="Search by title..." />
          <select
            className={`${inputClass} sm:w-52`}
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value)}
            aria-label="Filter by category"
          >
            <option value="">All categories</option>
            {categories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        </div>

        <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
          <div className="flex gap-2">
            <input
              value={newCategoryName}
              onChange={(e) => setNewCategoryName(e.target.value)}
              placeholder="New category name"
              className={`${inputClass} sm:w-44`}
            />
            <button
              type="button"
              onClick={handleAddCategory}
              disabled={addingCategory || !newCategoryName.trim()}
              className="rounded-md border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50 disabled:opacity-50"
            >
              + Category
            </button>
          </div>
          <button
            type="button"
            onClick={() => setUploading(true)}
            disabled={categories.length === 0}
            className="rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-700 disabled:opacity-50"
          >
            + Upload Document
          </button>
        </div>
      </div>

      <div className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
        {loading && <LoadingState />}
        {!loading && error && <ErrorState message={error} onRetry={refresh} />}
        {!loading && !error && data && (
          <>
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-slate-200">
                <thead className="bg-slate-50">
                  <tr>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                      Document
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                      Category
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                      Size
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                      Uploaded
                    </th>
                    <th className="px-4 py-3" />
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {data.content.length === 0 && (
                    <tr>
                      <td colSpan={5} className="px-4 py-8 text-center text-sm text-slate-500">
                        No documents found.
                      </td>
                    </tr>
                  )}
                  {data.content.map((document) => (
                    <tr key={document.id} className="hover:bg-slate-50">
                      <td className="px-4 py-3 text-sm font-medium text-slate-900">
                        <span className="mr-2" aria-hidden="true">
                          {FILE_ICON[document.contentType] ?? '📄'}
                        </span>
                        {document.title}
                      </td>
                      <td className="px-4 py-3 text-sm text-slate-600">{document.category.name}</td>
                      <td className="px-4 py-3 text-sm text-slate-500">{formatFileSize(document.fileSize)}</td>
                      <td className="px-4 py-3 text-sm text-slate-500">
                        {document.createdAt.slice(0, 10)}
                      </td>
                      <td className="px-4 py-3 text-right text-sm">
                        <a
                          href={knowledgeApi.downloadUrl(document.id)}
                          className="mr-3 font-medium text-slate-700 hover:text-slate-900"
                        >
                          Download
                        </a>
                        <button
                          type="button"
                          onClick={() => setDeleting(document)}
                          className="font-medium text-red-600 hover:text-red-800"
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <Pagination
              page={data.page}
              totalPages={data.totalPages}
              totalElements={data.totalElements}
              onPageChange={setPage}
            />
          </>
        )}
      </div>

      {uploading && (
        <Modal title="Upload Document" onClose={() => setUploading(false)}>
          <DocumentUploadForm categories={categories} onSubmit={handleUpload} onCancel={() => setUploading(false)} />
        </Modal>
      )}

      {deleting && (
        <ConfirmDialog
          title="Delete document"
          message={`Are you sure you want to delete "${deleting.title}"? This cannot be undone.`}
          onConfirm={handleDelete}
          onCancel={() => setDeleting(null)}
          busy={deleteBusy}
        />
      )}
      {deleteError && <p className="mt-2 text-sm text-red-600">{deleteError}</p>}
    </div>
  )
}
