import { useCallback, useEffect, useState } from 'react'
import { ApiError } from '../api/client'
import type { ListParams, PageResponse } from '../api/types'

export type ListState = ListParams

const DEFAULT_STATE: Required<Pick<ListState, 'q' | 'page' | 'size' | 'sortDir'>> = {
  q: '',
  page: 0,
  size: 10,
  sortDir: 'asc',
}

/**
 * Drives a paginated/searchable/sortable resource list. Shared by the
 * Departments, Patients, and Visits pages so each one only supplies its
 * own fetcher function instead of re-implementing this state machine.
 */
export function useResourceList<T, P extends ListState = ListState>(
  fetchPage: (params: P) => Promise<PageResponse<T>>,
  extraParams: Omit<P, keyof ListState> = {} as Omit<P, keyof ListState>,
) {
  const [state, setState] = useState<typeof DEFAULT_STATE & { sortBy?: string }>(DEFAULT_STATE)
  const [data, setData] = useState<PageResponse<T> | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [reloadToken, setReloadToken] = useState(0)

  const extraKey = JSON.stringify(extraParams)

  const load = useCallback(() => {
    setLoading(true)
    setError(null)
    const params = { ...state, ...extraParams } as P
    fetchPage(params)
      .then(setData)
      .catch((err: unknown) => {
        setError(err instanceof ApiError ? err.message : 'Failed to load data')
      })
      .finally(() => setLoading(false))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [state, extraKey, reloadToken])

  useEffect(() => {
    load()
  }, [load])

  const setQuery = (q: string) => setState((s) => ({ ...s, q, page: 0 }))
  const setPage = (page: number) => setState((s) => ({ ...s, page }))
  const setSort = (sortBy: string) =>
    setState((s) => ({
      ...s,
      sortBy,
      sortDir: s.sortBy === sortBy && s.sortDir === 'asc' ? 'desc' : 'asc',
    }))
  const refresh = () => setReloadToken((t) => t + 1)

  return { state, data, loading, error, setQuery, setPage, setSort, refresh }
}
