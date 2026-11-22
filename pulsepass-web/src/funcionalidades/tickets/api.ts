import { z } from 'zod'
import { pedir } from '../../lib/http'
import { TicketResponseSchema, type TipoTicket } from './tipos'

const ruta = (ticketCode: string) => `/tickets/${encodeURIComponent(ticketCode)}`

export const comprarTicket = (datos: { userEmail: string; eventCode: string; type: TipoTicket }) =>
    pedir('/tickets', TicketResponseSchema, { metodo: 'POST', cuerpo: datos })

export const obtenerTicket = (ticketCode: string) => pedir(ruta(ticketCode), TicketResponseSchema)

export const obtenerTicketsDeUsuario = (email: string) =>
    pedir(`/tickets/by-user?${new URLSearchParams({ email })}`, z.array(TicketResponseSchema))

export const cancelarTicket = (ticketCode: string) =>
    pedir(`${ruta(ticketCode)}/cancel`, TicketResponseSchema, { metodo: 'PATCH' })

export const usarTicket = (ticketCode: string) =>
    pedir(`${ruta(ticketCode)}/use`, TicketResponseSchema, { metodo: 'PATCH' })

export const obtenerTicketsPagados = (eventCode: string) =>
    pedir(`/events/${encodeURIComponent(eventCode)}/tickets/paid`, z.array(TicketResponseSchema))
