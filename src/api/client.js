export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://127.0.0.1:8080'

export class ApiError extends Error {
  constructor(message, status, code, details = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
    this.details = details
  }
}

export async function apiRequest(path, options = {}) {
  const {
    method = 'GET',
    body,
    token,
    headers = {}
  } = options

  const requestHeaders = {
    Accept: 'application/json',
    ...headers
  }

  if (token) {
    requestHeaders.Authorization = `Bearer ${token}`
  }

  const request = {
    method,
    headers: requestHeaders
  }

  if (body !== undefined) {
    requestHeaders['Content-Type'] = 'application/json'
    request.body = JSON.stringify(body)
  }

  const response = await fetch(apiUrl(path), request)

  if (response.status === 204) {
    return null
  }

  const data = await readJson(response)
  if (!response.ok) {
    throw new ApiError(
      data?.message || '请求失败，请稍后重试',
      response.status,
      data?.code || 'REQUEST_FAILED',
      data?.details || {}
    )
  }

  return data
}

export async function uploadFile(path, file, token) {
  const formData = new FormData()
  formData.append('file', file)

  const response = await fetch(apiUrl(path), {
    method: 'POST',
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    body: formData
  })

  const data = await readJson(response)
  if (!response.ok) {
    throw new ApiError(
      data?.message || '上传失败，请稍后重试',
      response.status,
      data?.code || 'UPLOAD_FAILED',
      data?.details || {}
    )
  }

  return data
}

export function apiUrl(path) {
  return path.startsWith('http') ? path : `${API_BASE_URL}${path}`
}

export function assetUrl(path) {
  if (!path) {
    return ''
  }

  if (path.startsWith('http') || path.startsWith('data:') || path.startsWith('blob:')) {
    return path
  }

  if (path.startsWith('/uploads/')) {
    return apiUrl(path)
  }

  return path
}

async function readJson(response) {
  const text = await response.text()
  if (!text) {
    return null
  }

  try {
    return JSON.parse(text)
  } catch (error) {
    return { message: text }
  }
}
