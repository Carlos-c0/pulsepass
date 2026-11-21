import { http, HttpResponse } from 'msw'
import {
    artistas,
    errorApi,
    eventoCMF,
    perfilAndrea,
    resumenCMF,
    resumenTech,
    sede,
    ticket,
    usuarios,
} from './datos'

// Respuestas por defecto de los 23 endpoints. Cada prueba cambia la que necesite con server.use()
const api = (ruta: string) => `*/api${ruta}`

function usuarioDelToken(request: Request) {
    const token = request.headers.get('Authorization')?.replace('Bearer ', '')
    if (token === 'token-USER') return usuarios.USER
    if (token === 'token-STAFF') return usuarios.STAFF
    if (token === 'token-ADMIN') return usuarios.ADMIN
    return null
}

export const handlers = [
    // Auth (F4)
    http.post(api('/auth/login'), () =>
        HttpResponse.json({ accessToken: 'token-USER', tokenType: 'Bearer', expiresIn: 900 }),
    ),
    http.get(api('/auth/me'), ({ request }) => {
        const usuario = usuarioDelToken(request)
        return usuario
            ? HttpResponse.json(usuario)
            : HttpResponse.json(errorApi(401, 'Authentication required'), { status: 401 })
    }),

    // Sedes
    http.get(api('/venues/active'), () => HttpResponse.json([sede])),
    http.get(api('/venues/:code'), ({ params }) =>
        params.code === sede.code
            ? HttpResponse.json(sede)
            : HttpResponse.json(errorApi(404, 'Venue not found'), { status: 404 }),
    ),

    // Eventos
    http.get(api('/events/published'), () => HttpResponse.json([resumenCMF, resumenTech])),
    http.get(api('/events/by-artist'), () => HttpResponse.json([resumenCMF])),
    http.get(api('/events/:eventCode/tickets/paid'), () =>
        HttpResponse.json([
            ticket(),
            ticket({
                ticketCode: 'TKT-91B07D',
                type: 'GENERAL',
                price: 120000,
                userEmail: 'carlos@pulsepass.test',
            }),
        ]),
    ),
    http.get(api('/events/:eventCode'), ({ params }) =>
        params.eventCode === eventoCMF.eventCode
            ? HttpResponse.json(eventoCMF)
            : HttpResponse.json(errorApi(404, 'Event not found'), { status: 404 }),
    ),
    http.post(api('/events'), async ({ request }) => {
        const cuerpo = (await request.json()) as Record<string, unknown>
        return HttpResponse.json(
            { ...eventoCMF, ...cuerpo, status: 'DRAFT', artists: [] },
            { status: 201 },
        )
    }),
    http.patch(api('/events/:eventCode/publish'), () =>
        HttpResponse.json({ ...eventoCMF, status: 'PUBLISHED' }),
    ),
    http.post(api('/events/:eventCode/artists/:artistId'), () =>
        HttpResponse.json({ ...eventoCMF, artists: artistas }),
    ),

    // Artistas
    http.get(api('/artists/active'), () => HttpResponse.json(artistas)),
    http.get(api('/artists/by-stage-name'), ({ request }) => {
        const nombre = new URL(request.url).searchParams.get('stageName')
        const artista = artistas.find((a) => a.stageName === nombre)
        return artista
            ? HttpResponse.json(artista)
            : HttpResponse.json(errorApi(404, 'Artist not found'), { status: 404 })
    }),
    http.get(api('/artists/:id'), ({ params }) => {
        const artista = artistas.find((a) => a.id === Number(params.id))
        return artista
            ? HttpResponse.json(artista)
            : HttpResponse.json(errorApi(404, 'Artist not found'), { status: 404 })
    }),

    // Usuarios
    http.post(api('/users'), () => HttpResponse.json(perfilAndrea, { status: 201 })),
    http.get(api('/users/by-email'), () => HttpResponse.json(perfilAndrea)),
    http.get(api('/users/by-username'), () => HttpResponse.json(perfilAndrea)),

    // Tickets
    http.post(api('/tickets'), () => HttpResponse.json(ticket(), { status: 201 })),
    http.get(api('/tickets/by-user'), () =>
        HttpResponse.json([
            ticket(),
            ticket({ ticketCode: 'TKT-USED01', status: 'USED', type: 'GENERAL', price: 120000 }),
            ticket({ ticketCode: 'TKT-CANC01', status: 'CANCELLED' }),
        ]),
    ),
    http.get(api('/tickets/:ticketCode'), ({ params }) =>
        params.ticketCode === 'TKT-8F2A1C'
            ? HttpResponse.json(ticket())
            : HttpResponse.json(errorApi(404, 'Ticket not found'), { status: 404 }),
    ),
    http.patch(api('/tickets/:ticketCode/cancel'), () =>
        HttpResponse.json(ticket({ status: 'CANCELLED' })),
    ),
    http.patch(api('/tickets/:ticketCode/use'), () => HttpResponse.json(ticket({ status: 'USED' }))),
]
