import type { ApiErrorBody } from './types'

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

export class ApiError extends Error {
  status: number
  details: string[]

  constructor(message: string, status: number, details: string[] = []) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.details = details
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const isFormData = init?.body instanceof FormData
  const response = await fetch(`${BASE_URL}${path}`, {
    ...init,
    headers: {
      // FormData sets its own multipart boundary header; forcing JSON here would break uploads.
      ...(isFormData ? {} : { 'Content-Type': 'application/json' }),
      ...init?.headers,
    },
  })

  if (response.status === 204) {
    return undefined as T
  }

  const isJson = response.headers.get('content-type')?.includes('application/json')
  const body = isJson ? await response.json() : undefined

  if (!response.ok) {
    const errorBody = body as ApiErrorBody | undefined
    throw new ApiError(
      errorBody?.message ?? `Request failed with status ${response.status}`,
      response.status,
      errorBody?.details ?? [],
    )
  }

  return body as T
}

export function buildQuery<T extends object>(params: T): string {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      search.set(key, String(value))
    }
  })
  const query = search.toString()
  return query ? `?${query}` : ''
}

export function fileUrl(path: string): string {
  return `${BASE_URL}${path}`
}

export const apiClient = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body: unknown) =>
    request<T>(path, { method: 'POST', body: JSON.stringify(body) }),
  postForm: <T>(path: string, formData: FormData) => request<T>(path, { method: 'POST', body: formData }),
  put: <T>(path: string, body: unknown) =>
    request<T>(path, { method: 'PUT', body: JSON.stringify(body) }),
  delete: (path: string) => request<void>(path, { method: 'DELETE' }),
}
