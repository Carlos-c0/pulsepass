import { screen } from '@testing-library/react'
import { http, HttpResponse } from 'msw'
import { describe, expect, it } from 'vitest'
import { errorApi } from '../test/mocks/datos'
import { peticionesA, server } from '../test/mocks/server'
import { renderConProveedores } from '../test/utils'

describe('Validación de ingreso (STAFF)', () => {
    it('TEST-FE-STF-001: buscar un ticket existente muestra evento, tipo y titular', async () => {
        const { user } = renderConProveedores({ ruta: '/staff/validacion', rol: 'STAFF' })

        await user.type(await screen.findByLabelText('Código de ticket'), 'tkt-8f2a1c')
        await user.click(screen.getByRole('button', { name: 'Buscar' }))

        expect(await screen.findByText('Caribbean Music Fest 2026')).toBeInTheDocument()
        expect(screen.getByText(/Titular: andrea@pulsepass.test/)).toBeInTheDocument()
        // el codigo se normaliza a mayusculas antes de pedirlo
        expect(peticionesA('GET', '/api/tickets/TKT-8F2A1C')).toHaveLength(1)
    })

    it('TEST-FE-STF-002: marcar como usado → 200 y aviso de ingreso registrado', async () => {
        const { user } = renderConProveedores({ ruta: '/staff/validacion', rol: 'STAFF' })

        await user.type(await screen.findByLabelText('Código de ticket'), 'TKT-8F2A1C')
        await user.click(screen.getByRole('button', { name: 'Buscar' }))
        await user.click(await screen.findByRole('button', { name: 'Marcar como usado' }))

        expect(await screen.findByText('Ingreso registrado.')).toBeInTheDocument()
        expect(peticionesA('PATCH', '/api/tickets/TKT-8F2A1C/use')).toHaveLength(1)
        expect(screen.getByLabelText('Código de ticket')).toHaveValue('')
    })

    it('TEST-FE-STF-003: un ticket ya usado (409) muestra el mensaje del backend', async () => {
        // El ticket se ve PAID, pero otra puerta lo uso un segundo antes
        server.use(
            http.patch('*/api/tickets/:ticketCode/use', () =>
                HttpResponse.json(errorApi(409, 'Only PAID tickets can be used.'), { status: 409 }),
            ),
        )
        const { user } = renderConProveedores({ ruta: '/staff/validacion', rol: 'STAFF' })

        await user.type(await screen.findByLabelText('Código de ticket'), 'TKT-8F2A1C')
        await user.click(screen.getByRole('button', { name: 'Buscar' }))
        await user.click(await screen.findByRole('button', { name: 'Marcar como usado' }))

        expect(await screen.findByText('Only PAID tickets can be used.')).toBeInTheDocument()
        expect(screen.queryByText('Ingreso registrado.')).not.toBeInTheDocument()
    })

    it('TEST-FE-STF-004: un código inexistente muestra "Ticket no encontrado."', async () => {
        const { user } = renderConProveedores({ ruta: '/staff/validacion', rol: 'STAFF' })

        await user.type(await screen.findByLabelText('Código de ticket'), 'TKT-NOEXISTE')
        await user.click(screen.getByRole('button', { name: 'Buscar' }))

        expect(await screen.findByText('Ticket no encontrado.')).toBeInTheDocument()
    })

    it('un USER que entra a la validación ve la página de sin permisos', async () => {
        renderConProveedores({ ruta: '/staff/validacion', rol: 'USER' })

        expect(
            await screen.findByRole('heading', {
                name: 'No tienes permisos para acceder a esta sección.',
            }),
        ).toBeInTheDocument()
    })

    it('la lista de tickets pagados muestra el total y permite usar uno', async () => {
        const { user } = renderConProveedores({ ruta: '/staff/eventos/CMF-2026/tickets', rol: 'STAFF' })

        expect(await screen.findByText('Total: 2')).toBeInTheDocument()
        await user.click(screen.getAllByRole('button', { name: 'Usar' })[0]!)

        expect(await screen.findByText('Ingreso registrado: TKT-8F2A1C')).toBeInTheDocument()
    })
})
