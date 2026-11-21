import { describe, expect, it } from 'vitest'
import { mensajeDeError } from './errores'
import { ApiError } from './http'

// QT-FE-005: cada codigo HTTP tiene su mensaje de interfaz (PRD seccion 13)
describe('lib/errores', () => {
    it.each([
        [0, 'No pudimos conectar con el servidor.'],
        [400, 'Request validation failed'],
        [401, 'Tu sesión expiró. Inicia sesión de nuevo.'],
        [403, 'No tienes permisos para hacer esto.'],
        [404, 'No encontramos lo que buscabas.'],
        [409, 'Request validation failed'],
        [500, 'Ocurrió un error inesperado. Inténtalo más tarde.'],
    ])('status %i → mensaje de UI', (status, esperado) => {
        expect(mensajeDeError(new ApiError(status, 'Request validation failed'))).toBe(esperado)
    })

    it('un 500 nunca muestra el mensaje técnico del servidor', () => {
        expect(mensajeDeError(new ApiError(500, 'NullPointerException at line 42'))).not.toContain(
            'Null',
        )
    })
})
