import { http, HttpResponse } from 'msw'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { z } from 'zod'
import { server } from '../test/mocks/server'
import { ApiError, pedir, registrarAlNoAutorizado } from './http'
import { guardarToken } from './sesionToken'

const Esquema = z.object({ ok: z.boolean() })

describe('lib/http', () => {
    afterEach(() => registrarAlNoAutorizado(null))

    it('TEST-FE-LIB-001: con sesión agrega Authorization Bearer (y nunca el token en la URL)', async () => {
        let cabecera: string | null = null
        let url = ''
        server.use(
            http.get('*/api/prueba', ({ request }) => {
                cabecera = request.headers.get('Authorization')
                url = request.url
                return HttpResponse.json({ ok: true })
            }),
        )
        guardarToken('abc.def.ghi', 900)

        await pedir('/prueba', Esquema)

        expect(cabecera).toBe('Bearer abc.def.ghi')
        expect(url).not.toContain('abc.def.ghi')
    })

    it('TEST-FE-LIB-002: un ErrorResponse válido se convierte en ApiError con status y details', async () => {
        server.use(
            http.get('*/api/prueba', () =>
                HttpResponse.json(
                    {
                        timestamp: 'x',
                        status: 400,
                        error: 'Bad Request',
                        message: 'Request validation failed',
                        details: { name: 'Name is required' },
                    },
                    { status: 400 },
                ),
            ),
        )

        const error = await pedir('/prueba', Esquema).catch((e: unknown) => e)

        expect(error).toBeInstanceOf(ApiError)
        expect(error).toMatchObject({
            status: 400,
            message: 'Request validation failed',
            details: { name: 'Name is required' },
        })
    })

    it('TEST-FE-LIB-003: un cuerpo de error que no es ErrorResponse da un ApiError genérico', async () => {
        server.use(
            http.get('*/api/prueba', () => new HttpResponse('<html>caido</html>', { status: 502 })),
        )

        const error = await pedir('/prueba', Esquema).catch((e: unknown) => e)

        expect(error).toBeInstanceOf(ApiError)
        expect(error).toMatchObject({ status: 502, details: {} })
        expect((error as ApiError).message).not.toContain('html')
    })

    it('TEST-FE-LIB-004: un 401 con token dispara el cierre de sesión', async () => {
        const alNoAutorizado = vi.fn()
        registrarAlNoAutorizado(alNoAutorizado)
        server.use(
            http.get('*/api/prueba', () =>
                HttpResponse.json(
                    {
                        timestamp: 'x',
                        status: 401,
                        error: 'Unauthorized',
                        message: 'Token expired',
                        details: {},
                    },
                    { status: 401 },
                ),
            ),
        )
        guardarToken('vencido', 900)

        await expect(pedir('/prueba', Esquema)).rejects.toBeInstanceOf(ApiError)
        expect(alNoAutorizado).toHaveBeenCalledOnce()
    })

    it('un 401 sin token (login fallido) no cierra ninguna sesión', async () => {
        const alNoAutorizado = vi.fn()
        registrarAlNoAutorizado(alNoAutorizado)
        server.use(
            http.get('*/api/prueba', () =>
                HttpResponse.json(
                    {
                        timestamp: 'x',
                        status: 401,
                        error: 'Unauthorized',
                        message: 'Invalid credentials',
                        details: {},
                    },
                    { status: 401 },
                ),
            ),
        )

        await expect(pedir('/prueba', Esquema)).rejects.toBeInstanceOf(ApiError)
        expect(alNoAutorizado).not.toHaveBeenCalled()
    })

    it('un fallo de red se convierte en ApiError con status 0', async () => {
        server.use(http.get('*/api/prueba', () => HttpResponse.error()))

        await expect(pedir('/prueba', Esquema)).rejects.toMatchObject({
            status: 0,
            message: 'No pudimos conectar con el servidor.',
        })
    })
})
