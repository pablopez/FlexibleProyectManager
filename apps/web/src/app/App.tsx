import { useEffect, useState, type FormEvent } from 'react'
import { getSetupStatus, initializeSetup, type InitializeSetupRequest } from '../shared/api/setupApi'

type LoadState =
  | { kind: 'loading' }
  | { kind: 'setup' }
  | { kind: 'initialized' }
  | { kind: 'error'; message: string }

type FormState = InitializeSetupRequest

const emptyForm: FormState = {
  organization: { name: '' },
  installation: { name: '' },
  administrator: { email: '', displayName: '', password: '' },
}

export function App() {
  const [state, setState] = useState<LoadState>({ kind: 'loading' })
  const [form, setForm] = useState<FormState>(emptyForm)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    getSetupStatus()
      .then((setup) => setState(setup.initialized ? { kind: 'initialized' } : { kind: 'setup' }))
      .catch((error: unknown) => {
        const message = error instanceof Error ? error.message : 'Unable to reach the backend.'
        setState({ kind: 'error', message })
      })
  }, [])

  function updateForm(section: keyof FormState, field: string, value: string) {
    setForm((current) => ({ ...current, [section]: { ...current[section], [field]: value } }))
  }

  async function submitSetup(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitting(true)
    try {
      await initializeSetup(form)
      setState({ kind: 'initialized' })
    } catch (error: unknown) {
      setState({ kind: 'error', message: error instanceof Error ? error.message : 'Setup failed.' })
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main>
      <h1>Flexible Project Manager</h1>
      {state.kind === 'loading' && <p>Checking system connectivity…</p>}
      {state.kind === 'error' && <p role="alert">Backend unavailable: {state.message}</p>}
      {state.kind === 'initialized' && <p role="status">This installation is already initialized. Login will be available in a future release.</p>}
      {state.kind === 'setup' && (
        <form onSubmit={submitSetup} aria-label="First-run setup">
          <h2>Initialize your installation</h2>
          <label>Organization name<input required maxLength={200} value={form.organization.name} onChange={(event) => updateForm('organization', 'name', event.target.value)} /></label>
          <label>Installation name<input required maxLength={200} value={form.installation.name} onChange={(event) => updateForm('installation', 'name', event.target.value)} /></label>
          <label>Administrator email<input required type="email" value={form.administrator.email} onChange={(event) => updateForm('administrator', 'email', event.target.value)} /></label>
          <label>Administrator display name<input required maxLength={200} value={form.administrator.displayName} onChange={(event) => updateForm('administrator', 'displayName', event.target.value)} /></label>
          <label>Administrator password<input required minLength={8} type="password" value={form.administrator.password} onChange={(event) => updateForm('administrator', 'password', event.target.value)} /></label>
          <button type="submit" disabled={submitting}>{submitting ? 'Initializing…' : 'Initialize installation'}</button>
        </form>
      )}
    </main>
  )
}
