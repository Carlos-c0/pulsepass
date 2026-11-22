import { useState } from 'react'
import { z } from 'zod'

// La API no lista todos los eventos (GAP-01). Los creados en esta pestana se recuerdan aqui
// y se pierden al cerrarla: es una limitacion documentada en el PRD (9.16).
const CLAVE = 'pulsepass.eventosCreados'

const EventoCreadoSchema = z.object({ eventCode: z.string(), name: z.string() })
export type EventoCreado = z.infer<typeof EventoCreadoSchema>

function leer(): EventoCreado[] {
    try {
        const datos: unknown = JSON.parse(sessionStorage.getItem(CLAVE) ?? '[]')
        const lista = z.array(EventoCreadoSchema).safeParse(datos)
        return lista.success ? lista.data : []
    } catch {
        return []
    }
}

export function recordarEventoCreado(evento: EventoCreado): void {
    const lista = [evento, ...leer().filter((e) => e.eventCode !== evento.eventCode)]
    try {
        sessionStorage.setItem(CLAVE, JSON.stringify(lista))
    } catch {
        // sin almacenamiento solo se pierde la lista de esta pestana
    }
}

export function useEventosCreados(): EventoCreado[] {
    const [lista] = useState(leer)
    return lista
}
