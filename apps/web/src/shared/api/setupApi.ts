export type SetupStatusResponse = { initialized: boolean }

export type InitializeSetupRequest = {
  organization: { name: string }
  installation: { name: string }
  administrator: { email: string; displayName: string; password: string }
}

export type SetupInitializationResponse = {
  initialized: true
  organization: { id: string; name: string }
  installation: {
    id: string
    organizationId: string
    name: string
    platform: string
    applicationVersion: string
    status: string
    createdAt: string
    lastSeenAt: string | null
  }
  administrator: { id: string; email: string; displayName: string }
}

type ApiError = { code?: string; message?: string }

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? '/api/v1'

async function readResponse<T>(response: Response): Promise<T> {
  const body = (await response.json()) as T & ApiError
  if (!response.ok) {
    throw new Error(body.message ?? `Setup request failed with HTTP ${response.status}.`)
  }
  return body
}

export async function getSetupStatus(): Promise<SetupStatusResponse> {
  return readResponse(await fetch(`${apiBaseUrl}/setup/status`, { headers: { Accept: 'application/json' } }))
}

export async function initializeSetup(request: InitializeSetupRequest): Promise<SetupInitializationResponse> {
  return readResponse(await fetch(`${apiBaseUrl}/setup/initialize`, {
    method: 'POST',
    headers: { Accept: 'application/json', 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  }))
}
