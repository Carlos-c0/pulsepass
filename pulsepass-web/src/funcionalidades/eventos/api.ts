import { z } from 'zod'
import { pedir } from '../../lib/http'
import { EventResponseSchema, EventSummaryResponseSchema, type CrearEventoForm } from './tipos'

const ruta = (eventCode: string) => `/events/${encodeURIComponent(eventCode)}`

export const obtenerEventosPublicados = () =>
    pedir('/events/published', z.array(EventSummaryResponseSchema))

export const obtenerEvento = (eventCode: string) => pedir(ruta(eventCode), EventResponseSchema)

export const obtenerEventosPorArtista = (stageName: string) =>
    pedir(
        `/events/by-artist?${new URLSearchParams({ stageName })}`,
        z.array(EventSummaryResponseSchema),
    )

export const crearEvento = (datos: CrearEventoForm) =>
    pedir('/events', EventResponseSchema, {
        metodo: 'POST',
        cuerpo: { ...datos, description: datos.description.trim() || null },
    })

export const publicarEvento = (eventCode: string) =>
    pedir(`${ruta(eventCode)}/publish`, EventResponseSchema, { metodo: 'PATCH' })

export const asociarArtista = (eventCode: string, artistId: number) =>
    pedir(`${ruta(eventCode)}/artists/${artistId}`, EventResponseSchema, { metodo: 'POST' })
