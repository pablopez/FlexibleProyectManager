import { useEffect, useState } from 'react'
import { getHealth, type HealthResponse } from '../shared/api/healthApi'

type LoadState =
  | { kind: 'loading' }
  | { kind: 'success'; health: HealthResponse }
  | { kind: 'error'; message: string }

export function App() {
  const [state, setState] = useState<LoadState>({ kind: 'loading' })

  useEffect(() => {
    getHealth()
      .then((health) => setState({ kind: 'success', health }))
      .catch((error: unknown) => {
        const message = error instanceof Error ? error.message : 'Unable to reach the backend.'
        setState({ kind: 'error', message })
      })
  }, [])

  return (
    <main>
      <h1>Flexible Project Manager</h1>
      {state.kind === 'loading' && <p>Checking system connectivity…</p>}
      {state.kind === 'error' && <p role="alert">Backend unavailable: {state.message}</p>}
      {state.kind === 'success' && (
        <section aria-label="System connectivity">
          <p>Backend {state.health.backend === 'UP' ? 'connected' : 'unavailable'}</p>
          <p>Database {state.health.database === 'UP' ? 'connected' : 'unavailable'}</p>
        </section>
      )}
    </main>
  )
}
