import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useAuth } from '../auth/AuthContext'
import {
    cancelarTicket,
    comprarTicket,
    obtenerTicket,
    obtenerTicketsDeUsuario,
    obtenerTicketsPagados,
    usarTicket,
} from './api'
import type { Ticket, TipoTicket } from './tipos'

const QUINCE_SEGUNDOS = 15 * 1000

// El email sale de la sesion, nunca de un campo que la persona pueda editar (24.3)
function useEmailSesion(): string {
    const { sesion } = useAuth()
    return sesion.estado === 'autenticado' ? sesion.usuario.email : ''
}

export function useMisTickets() {
    const email = useEmailSesion()
    return useQuery({
        queryKey: ['tickets', 'usuario', email],
        queryFn: () => obtenerTicketsDeUsuario(email),
        staleTime: QUINCE_SEGUNDOS,
        enabled: email !== '',
    })
}

export function useTicket(ticketCode: string) {
    return useQuery({
        queryKey: ['ticket', ticketCode],
        queryFn: () => obtenerTicket(ticketCode),
        staleTime: QUINCE_SEGUNDOS,
        enabled: ticketCode !== '',
    })
}

// Para la taquilla: la busqueda se hace cuando se envia el codigo
export const useBuscarTicket = useTicket

export function useTicketsPagadosPorEvento(eventCode: string) {
    return useQuery({
        queryKey: ['tickets', 'pagados', eventCode],
        queryFn: () => obtenerTicketsPagados(eventCode),
        staleTime: QUINCE_SEGUNDOS,
        enabled: eventCode !== '',
    })
}

export function useComprarTicket() {
    const queryClient = useQueryClient()
    const email = useEmailSesion()
    return useMutation({
        mutationFn: ({ eventCode, type }: { eventCode: string; type: TipoTicket }) =>
            comprarTicket({ userEmail: email, eventCode, type }),
        onSuccess: (ticket) => {
            queryClient.invalidateQueries({ queryKey: ['tickets', 'usuario', email] })
            queryClient.invalidateQueries({ queryKey: ['evento', ticket.eventCode] })
        },
    })
}

function useActualizarTicket(accion: (ticketCode: string) => Promise<Ticket>) {
    const queryClient = useQueryClient()
    const email = useEmailSesion()
    return useMutation({
        mutationFn: accion,
        onSuccess: (ticket) => {
            queryClient.setQueryData(['ticket', ticket.ticketCode], ticket)
            queryClient.invalidateQueries({ queryKey: ['tickets', 'usuario', email] })
            queryClient.invalidateQueries({ queryKey: ['tickets', 'pagados', ticket.eventCode] })
        },
    })
}

export const useCancelarTicket = () => useActualizarTicket(cancelarTicket)
export const useUsarTicket = () => useActualizarTicket(usarTicket)
