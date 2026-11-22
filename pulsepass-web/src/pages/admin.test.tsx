import { fireEvent, screen, waitFor, within } from '@testing-library/react'
import { http, HttpResponse } from 'msw'
import { describe, expect, it } from 'vitest'
import { errorApi, eventoCMF } from '../test/mocks/datos'
import { peticionesA, server } from '../test/mocks/server'
import { renderConProveedores } from '../test/utils'

async function llenarFormulario(user: ReturnType<typeof renderConProveedores>['user']) {
    await user.type(await screen.findByLabelText('Código del evento'), 'jzz-2026')
    await user.type(screen.getByLabelText('Nombre'), 'Jazz al Parque')
    await user.selectOptions(screen.getByLabelText('Categoría'), 'CULTURE')
    await user.clear(screen.getByLabelText('Edad mínima'))
    await user.type(screen.getByLabelText('Edad mínima'), '18')
    fireEvent.change(screen.getByLabelText('Fecha y hora'), { target: { value: '2026-12-20T20:00' } })
    const sede = screen.getByLabelText('Sede')
    await within(sede).findByRole('option', { name: /VEN-SMR-01/ })
    await user.selectOptions(sede, 'VEN-SMR-01')
}

describe('Administración de eventos (ADMIN)', () => {
    it('TEST-FE-ADM-001: crear un evento válido → 201, queda en DRAFT y abre su gestión', async () => {
        const { user, router } = renderConProveedores({ ruta: '/admin/eventos/nuevo', rol: 'ADMIN' })

        await llenarFormulario(user)
        await user.click(screen.getByRole('button', { name: 'Crear evento' }))

        expect(await screen.findByRole('heading', { name: /Jazz al Parque/ })).toBeInTheDocument()
        expect(router.state.location.pathname).toBe('/admin/eventos/JZZ-2026')
        expect(screen.getByText('DRAFT')).toBeInTheDocument()
    })

    it('TEST-FE-ADM-002: los campos vacíos se validan sin llamar a la API', async () => {
        const { user } = renderConProveedores({ ruta: '/admin/eventos/nuevo', rol: 'ADMIN' })

        await user.click(await screen.findByRole('button', { name: 'Crear evento' }))

        expect(await screen.findByText('Ingresa el código del evento')).toBeInTheDocument()
        expect(screen.getByText('Elige la sede')).toBeInTheDocument()
        expect(peticionesA('POST', '/api/events')).toHaveLength(0)
    })

    it('TEST-FE-ADM-003: un 400 con details marca el campo correspondiente', async () => {
        server.use(
            http.post('*/api/events', () =>
                HttpResponse.json(
                    errorApi(400, 'Validation failed', { eventDate: 'must be a future date' }),
                    {
                        status: 400,
                    },
                ),
            ),
        )
        const { user } = renderConProveedores({ ruta: '/admin/eventos/nuevo', rol: 'ADMIN' })

        await llenarFormulario(user)
        await user.click(screen.getByRole('button', { name: 'Crear evento' }))

        expect(await screen.findByText('must be a future date')).toBeInTheDocument()
        expect(screen.getByLabelText('Fecha y hora')).toHaveAttribute('aria-invalid', 'true')
    })

    it('TEST-FE-ADM-004: un código repetido (409) se muestra arriba del formulario', async () => {
        server.use(
            http.post('*/api/events', () =>
                HttpResponse.json(errorApi(409, 'Event code already exists.'), { status: 409 }),
            ),
        )
        const { user } = renderConProveedores({ ruta: '/admin/eventos/nuevo', rol: 'ADMIN' })

        await llenarFormulario(user)
        await user.click(screen.getByRole('button', { name: 'Crear evento' }))

        expect(await screen.findByText('Event code already exists.')).toBeInTheDocument()
    })

    it('TEST-FE-ADM-005: publicar un DRAFT pide confirmación y cambia el badge', async () => {
        let estado = 'DRAFT'
        server.use(
            http.get('*/api/events/:eventCode', () =>
                HttpResponse.json({ ...eventoCMF, status: estado }),
            ),
            http.patch('*/api/events/:eventCode/publish', () => {
                estado = 'PUBLISHED'
                return HttpResponse.json({ ...eventoCMF, status: estado })
            }),
        )
        const { user } = renderConProveedores({ ruta: '/admin/eventos/CMF-2026', rol: 'ADMIN' })

        await user.click(await screen.findByRole('button', { name: 'Publicar evento' }))
        const modal = await screen.findByRole('dialog', { name: 'Publicar evento' })
        await user.click(within(modal).getByRole('button', { name: 'Publicar' }))

        expect(await screen.findByText('Evento publicado.')).toBeInTheDocument()
        await waitFor(() =>
            expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent('PUBLISHED'),
        )
        expect(screen.getByRole('button', { name: 'Publicar evento' })).toBeDisabled()
    })

    it('TEST-FE-ADM-006: asociar un artista lo agrega a la lista', async () => {
        let artistas = eventoCMF.artists.slice(0, 1)
        server.use(
            http.get('*/api/events/:eventCode', () =>
                HttpResponse.json({ ...eventoCMF, status: 'DRAFT', artists: artistas }),
            ),
            http.post('*/api/events/:eventCode/artists/:artistId', () => {
                artistas = eventoCMF.artists
                return HttpResponse.json({ ...eventoCMF, status: 'DRAFT', artists: artistas })
            }),
        )
        const { user } = renderConProveedores({ ruta: '/admin/eventos/CMF-2026', rol: 'ADMIN' })

        const lista = await screen.findByRole('list', { name: 'Artistas asociados' })
        expect(within(lista).getAllByRole('listitem')).toHaveLength(1)
        const selector = screen.getByLabelText('Agregar artista')
        await within(selector).findByRole('option', { name: 'Neon Waves' })
        await user.selectOptions(selector, 'Neon Waves')
        await user.click(screen.getByRole('button', { name: 'Asociar' }))

        expect(await screen.findByText('Neon Waves quedó asociado al evento.')).toBeInTheDocument()
        await waitFor(() =>
            expect(
                within(screen.getByRole('list', { name: 'Artistas asociados' })).getAllByRole('listitem'),
            ).toHaveLength(2),
        )
    })

    it('un STAFF no puede entrar a la administración', async () => {
        renderConProveedores({ ruta: '/admin/eventos', rol: 'STAFF' })

        expect(
            await screen.findByRole('heading', {
                name: 'No tienes permisos para acceder a esta sección.',
            }),
        ).toBeInTheDocument()
    })
})
