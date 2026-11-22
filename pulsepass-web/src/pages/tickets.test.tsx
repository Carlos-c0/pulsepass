import { screen, waitFor, within } from '@testing-library/react'
import { http, HttpResponse } from 'msw'
import { describe, expect, it } from 'vitest'
import { errorApi, ticket } from '../test/mocks/datos'
import { peticionesA, server } from '../test/mocks/server'
import { renderConProveedores } from '../test/utils'

describe('Compra', () => {
    it('TEST-FE-TKT-001: compra válida → 201 y navega a la confirmación', async () => {
        const { user, router } = renderConProveedores({ ruta: '/eventos/CMF-2026', rol: 'USER' })

        await user.click(await screen.findByLabelText('VIP'))
        await user.click(screen.getAllByRole('button', { name: 'Comprar entrada' })[0]!)
        const modal = await screen.findByRole('dialog', { name: 'Confirmar compra' })
        await user.click(within(modal).getByRole('button', { name: 'Confirmar' }))

        expect(
            await screen.findByRole('heading', { name: '¡Compra realizada con éxito!' }),
        ).toBeInTheDocument()
        expect(router.state.location.pathname).toBe('/compra/confirmacion/TKT-8F2A1C')
    })

    it('TEST-FE-TKT-002: un anónimo que quiere comprar va a /login y luego vuelve al evento', async () => {
        const { user, router } = renderConProveedores({ ruta: '/eventos/CMF-2026' })

        await user.click(
            (await screen.findAllByRole('button', { name: 'Inicia sesión para comprar' }))[0]!,
        )

        await waitFor(() => expect(router.state.location.pathname).toBe('/login'))
        expect(router.state.location.state).toMatchObject({ desde: '/eventos/CMF-2026' })

        await user.type(await screen.findByLabelText('Usuario o correo'), 'andrea')
        await user.type(screen.getByLabelText('Contraseña'), 'ClaveSegura2026!')
        await user.click(screen.getByRole('button', { name: 'Ingresar' }))
        expect(
            await screen.findByRole('heading', { name: 'Caribbean Music Fest 2026' }),
        ).toBeInTheDocument()
        expect(router.state.location.pathname).toBe('/eventos/CMF-2026')
    })

    it('TEST-FE-TKT-003: un 409 (edad mínima) se muestra en el modal y no navega', async () => {
        server.use(
            http.post('*/api/tickets', () =>
                HttpResponse.json(errorApi(409, 'User does not meet minimum age.'), { status: 409 }),
            ),
        )
        const { user, router } = renderConProveedores({ ruta: '/eventos/CMF-2026', rol: 'USER' })

        await user.click((await screen.findAllByRole('button', { name: 'Comprar entrada' }))[0]!)
        const modal = await screen.findByRole('dialog', { name: 'Confirmar compra' })
        await user.click(within(modal).getByRole('button', { name: 'Confirmar' }))

        expect(await within(modal).findByRole('alert')).toHaveTextContent(
            'User does not meet minimum age.',
        )
        expect(router.state.location.pathname).toBe('/eventos/CMF-2026')
    })

    it('TEST-FE-TKT-004: la compra envía el email de la sesión y no el precio', async () => {
        let cuerpo: unknown
        server.use(
            http.post('*/api/tickets', async ({ request }) => {
                cuerpo = await request.json()
                return HttpResponse.json(ticket(), { status: 201 })
            }),
        )
        const { user } = renderConProveedores({ ruta: '/eventos/CMF-2026', rol: 'USER' })

        await user.click(await screen.findByLabelText('BACKSTAGE'))
        await user.click(screen.getAllByRole('button', { name: 'Comprar entrada' })[0]!)
        await user.click(
            within(await screen.findByRole('dialog')).getByRole('button', { name: 'Confirmar' }),
        )
        await screen.findByRole('heading', { name: '¡Compra realizada con éxito!' })

        expect(cuerpo).toEqual({
            userEmail: 'andrea@pulsepass.test',
            eventCode: 'CMF-2026',
            type: 'BACKSTAGE',
        })
    })
})

describe('Mis tickets', () => {
    it('TEST-FE-TKT-005: lista los tickets propios y filtra por estado', async () => {
        const { user } = renderConProveedores({ ruta: '/mis-tickets', rol: 'USER' })

        expect(await screen.findAllByRole('article')).toHaveLength(3)
        await user.click(screen.getByRole('button', { name: 'Usados' }))

        expect(screen.getByRole('button', { name: 'Usados' })).toHaveAttribute('aria-pressed', 'true')
        expect(screen.getAllByRole('article')).toHaveLength(1)
        expect(screen.getByText('TKT-USED01')).toBeInTheDocument()
    })

    it('TEST-FE-TKT-006: sin tickets muestra el estado vacío', async () => {
        server.use(http.get('*/api/tickets/by-user', () => HttpResponse.json([])))
        renderConProveedores({ ruta: '/mis-tickets', rol: 'USER' })

        expect(await screen.findByText('Aún no tienes tickets')).toBeInTheDocument()
        expect(screen.getByRole('link', { name: 'Ver la cartelera' })).toBeInTheDocument()
    })

    it('TEST-FE-TKT-007: cancelar un ticket PAID → 200 y la lista se actualiza', async () => {
        let cancelado = false
        server.use(
            http.patch('*/api/tickets/:ticketCode/cancel', () => {
                cancelado = true
                return HttpResponse.json(ticket({ status: 'CANCELLED' }))
            }),
            http.get('*/api/tickets/by-user', () =>
                HttpResponse.json([ticket({ status: cancelado ? 'CANCELLED' : 'PAID' })]),
            ),
        )
        const { user } = renderConProveedores({ ruta: '/mis-tickets', rol: 'USER' })

        await user.click(await screen.findByRole('button', { name: 'Cancelar' }))
        await user.click(
            within(await screen.findByRole('dialog')).getByRole('button', { name: 'Cancelar ticket' }),
        )

        // QT-FE-008: la invalidacion vuelve a pedir la lista y aparece el nuevo estado
        expect(await screen.findByText('CANCELLED')).toBeInTheDocument()
        expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
        expect(peticionesA('GET', '/api/tickets/by-user').length).toBeGreaterThanOrEqual(2)
    })

    it('TEST-FE-TKT-008: un 409 al cancelar se muestra y el modal sigue abierto', async () => {
        server.use(
            http.patch('*/api/tickets/:ticketCode/cancel', () =>
                HttpResponse.json(errorApi(409, 'Only PAID tickets can be cancelled.'), { status: 409 }),
            ),
        )
        const { user } = renderConProveedores({ ruta: '/mis-tickets', rol: 'USER' })

        await user.click(await screen.findByRole('button', { name: 'Cancelar' }))
        const modal = await screen.findByRole('dialog')
        await user.click(within(modal).getByRole('button', { name: 'Cancelar ticket' }))

        expect(await within(modal).findByRole('alert')).toHaveTextContent(
            'Only PAID tickets can be cancelled.',
        )
        expect(screen.getByRole('dialog')).toBeInTheDocument()
    })

    it('TEST-FE-TKT-009: un ticket ajeno (403) se ve como "Ticket no disponible"', async () => {
        server.use(
            http.get('*/api/tickets/:ticketCode', () =>
                HttpResponse.json(errorApi(403, 'Access denied'), { status: 403 }),
            ),
        )
        renderConProveedores({ ruta: '/mis-tickets/TKT-CARLOS', rol: 'USER' })

        expect(await screen.findByRole('heading', { name: 'Ticket no disponible' })).toBeInTheDocument()
        expect(screen.queryByText('TKT-CARLOS')).not.toBeInTheDocument()
    })

    it('TEST-FE-TKT-010: "Cancelar" solo aparece en tickets PAID', async () => {
        renderConProveedores({ ruta: '/mis-tickets', rol: 'USER' })

        const tarjetas = await screen.findAllByRole('article')
        const conBoton = tarjetas.filter((t) => within(t).queryByRole('button', { name: 'Cancelar' }))
        expect(conBoton).toHaveLength(1)
        expect(within(conBoton[0]!).getByText('PAID')).toBeInTheDocument()
    })
})
