import { screen, within } from '@testing-library/react'
import { http, HttpResponse } from 'msw'
import { describe, expect, it } from 'vitest'
import { errorApi, eventoCMF, resumenCMF } from '../test/mocks/datos'
import { peticionesA, server } from '../test/mocks/server'
import { renderConProveedores } from '../test/utils'

describe('Cartelera', () => {
    it('TEST-FE-EVT-001: un anónimo ve los eventos publicados sin iniciar sesión', async () => {
        renderConProveedores({ ruta: '/eventos' })

        expect(
            await screen.findByRole('link', { name: /Caribbean Music Fest 2026/ }),
        ).toBeInTheDocument()
        expect(screen.getByRole('link', { name: /Tech Summit Caribe/ })).toBeInTheDocument()
        expect(screen.getByText('2 eventos')).toBeInTheDocument()
    })

    it('TEST-FE-EVT-002: sin eventos publicados muestra el estado vacío', async () => {
        server.use(http.get('*/api/events/published', () => HttpResponse.json([])))
        renderConProveedores({ ruta: '/eventos' })

        expect(await screen.findByText('Aún no hay eventos publicados')).toBeInTheDocument()
    })

    it('TEST-FE-EVT-003: el filtro por texto y categoría queda en la URL', async () => {
        const { user, router } = renderConProveedores({ ruta: '/eventos' })
        await screen.findByText('2 eventos')

        await user.type(screen.getByLabelText('Buscar por nombre'), 'caribbean')
        expect(await screen.findByText('1 evento')).toBeInTheDocument()

        await user.selectOptions(screen.getByLabelText('Categoría'), 'TECHNOLOGY')
        expect(await screen.findByText('Sin resultados para tu búsqueda')).toBeInTheDocument()
        expect(router.state.location.search).toBe('?q=caribbean&categoria=TECHNOLOGY')
    })

    it('con ?artista= consulta los eventos de ese artista', async () => {
        renderConProveedores({ ruta: '/eventos?artista=Solar%20Beat' })

        expect(await screen.findByText('1 evento')).toBeInTheDocument()
        expect(peticionesA('GET', '/api/events/by-artist')).toHaveLength(1)
        expect(await screen.findByRole('link', { name: 'Solar Beat' })).toHaveAttribute(
            'href',
            '/artistas/1',
        )
    })
})

describe('Detalle de evento', () => {
    it('TEST-FE-EVT-004: muestra el evento, sus artistas y la sede', async () => {
        renderConProveedores({ ruta: '/eventos/CMF-2026' })

        expect(
            await screen.findByRole('heading', { name: 'Caribbean Music Fest 2026' }),
        ).toBeInTheDocument()
        expect(screen.getByText('Edad mínima: 18')).toBeInTheDocument()
        expect(screen.getByRole('link', { name: 'Solar Beat' })).toBeInTheDocument()
        expect(screen.getByRole('link', { name: 'Neon Waves' })).toBeInTheDocument()
        expect(screen.getByRole('link', { name: 'Marina Convention Center' })).toHaveAttribute(
            'href',
            '/sedes/VEN-SMR-01',
        )
        expect(await screen.findByText('Santa Marta · Capacidad 3')).toBeInTheDocument()
    })

    it('TEST-FE-EVT-005: un 404 muestra "Evento no disponible"', async () => {
        renderConProveedores({ ruta: '/eventos/NO-EXISTE' })

        expect(await screen.findByRole('heading', { name: 'Evento no disponible' })).toBeInTheDocument()
    })

    it('TEST-FE-EVT-006: un evento SOLD_OUT no deja comprar', async () => {
        server.use(
            http.get('*/api/events/:eventCode', () =>
                HttpResponse.json({ ...eventoCMF, status: 'SOLD_OUT' }),
            ),
        )
        renderConProveedores({ ruta: '/eventos/CMF-2026', rol: 'USER' })

        expect(await screen.findByText('Entradas agotadas.')).toBeInTheDocument()
        for (const boton of screen.getAllByRole('button', { name: 'Comprar entrada' })) {
            expect(boton).toBeDisabled()
        }
    })

    it('STAFF y ADMIN no ven el panel de compra', async () => {
        renderConProveedores({ ruta: '/eventos/CMF-2026', rol: 'STAFF' })

        await screen.findByRole('heading', { name: 'Caribbean Music Fest 2026' })
        expect(screen.queryByRole('heading', { name: 'Entradas' })).not.toBeInTheDocument()
    })
})

describe('Artistas', () => {
    it('TEST-FE-ART-001: lista los artistas activos', async () => {
        renderConProveedores({ ruta: '/artistas' })

        const lista = await screen.findByRole('list')
        expect(within(lista).getAllByRole('link')).toHaveLength(3)
        expect(within(lista).getByText('Caribbean Sound')).toBeInTheDocument()
    })

    it('TEST-FE-ART-002: el detalle muestra el artista y sus eventos', async () => {
        renderConProveedores({ ruta: '/artistas/1' })

        expect(await screen.findByRole('heading', { name: 'Solar Beat' })).toBeInTheDocument()
        expect(
            await screen.findByRole('link', { name: new RegExp(resumenCMF.name) }),
        ).toBeInTheDocument()
    })

    it('TEST-FE-ART-003: un id que no es número da 404 sin llamar a la API', async () => {
        renderConProveedores({ ruta: '/artistas/abc' })

        expect(
            await screen.findByRole('heading', { name: 'No encontramos esta página.' }),
        ).toBeInTheDocument()
        expect(peticionesA('GET', '/api/artists/abc')).toHaveLength(0)
    })
})

describe('Sedes', () => {
    it('TEST-FE-VEN-001: lista, detalle y 404 de sede', async () => {
        const { user } = renderConProveedores({ ruta: '/sedes' })

        await user.click(await screen.findByRole('link', { name: /Marina Convention Center/ }))
        expect(await screen.findByText('3 personas')).toBeInTheDocument()

        server.use(
            http.get('*/api/venues/:code', () =>
                HttpResponse.json(errorApi(404, 'Venue not found'), { status: 404 }),
            ),
        )
        renderConProveedores({ ruta: '/sedes/NO-EXISTE' })
        expect(
            await screen.findByRole('heading', { name: 'No encontramos esta página.' }),
        ).toBeInTheDocument()
    })
})
