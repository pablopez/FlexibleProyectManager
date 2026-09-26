import { afterEach, describe, expect, it, vi } from 'vitest'
import { getHealth } from './healthApi'

describe('getHealth', () => {
  afterEach(() => vi.restoreAllMocks())

  it('returns the healthy backend response', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify({
        status: 'UP',
        backend: 'UP',
        database: 'UP',
        timestamp: '2026-09-26T11:00:00Z',
      }), { status: 200, headers: { 'Content-Type': 'application/json' } }),
    )

    await expect(getHealth()).resolves.toMatchObject({ status: 'UP', backend: 'UP', database: 'UP' })
  })

  it('rejects when the backend returns an unavailable response', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify({
        status: 'DOWN',
        backend: 'UP',
        database: 'DOWN',
        timestamp: '2026-09-26T11:00:00Z',
      }), { status: 503, headers: { 'Content-Type': 'application/json' } }),
    )

    await expect(getHealth()).rejects.toThrow('HTTP 503')
  })
})
