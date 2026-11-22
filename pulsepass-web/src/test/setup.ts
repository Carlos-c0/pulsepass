import '@testing-library/jest-dom/vitest'
import { cleanup, configure } from '@testing-library/react'
import { afterAll, afterEach, beforeAll } from 'vitest'
import { borrarToken } from '../lib/sesionToken'
import { peticiones, server } from './mocks/server'

// La primera prueba de cada archivo descarga la pagina lazy; en equipos lentos tarda mas de 1 s
configure({ asyncUtilTimeout: 5000 })

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