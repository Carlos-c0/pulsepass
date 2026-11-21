import '@testing-library/jest-dom/vitest'
import { cleanup } from '@testing-library/react'
import { afterAll, afterEach, beforeAll } from 'vitest'
import { borrarToken } from '../lib/sesionToken'
import { peticiones, server } from './mocks/server'

// onUnhandledFrame 'error': si un componente llama a un endpoint sin mock, la prueba falla
beforeAll(() => server.listen({ onUnhandledFrame: 'error' }))

afterEach(() => {
    cleanup()
    server.resetHandlers()
    peticiones.length = 0
    borrarToken()
    sessionStorage.clear()
})

afterAll(() => server.close())
