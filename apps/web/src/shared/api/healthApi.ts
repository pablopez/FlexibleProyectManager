export type HealthStatus = 'UP' | 'DOWN'

export type HealthResponse = {
  status: HealthStatus
  backend: HealthStatus
  database: HealthStatus
  timestamp: string
}

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? '/api/v1'

export async function getHealth(): Promise<HealthResponse> {
  const response = await fetch(`${apiBaseUrl}/system/health`, {
    headers: { Accept: 'application/json' },
  })

  const body = (await response.json()) as HealthResponse
  if (!response.ok) {
    throw new Error(`Health check failed with HTTP ${response.status}.`)
  }

  return body
}
