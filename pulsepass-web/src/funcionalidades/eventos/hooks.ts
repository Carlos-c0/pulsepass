import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
    asociarArtista,
    crearEvento,
    obtenerEvento,
    obtenerEventosPorArtista,
    obtenerEventosPublicados,
    publicarEvento,
} from './api'

const UN_MINUTO = 60 * 1000

export function useEventosPublicados() {
    return useQuery({
        queryKey: ['eventos', 'publicados'],
        queryFn: obtenerEventosPublicados,
        staleTime: UN_MINUTO,
    })
}

export function useEvento(eventCode: string) {
    return useQuery({
        queryKey: ['evento', eventCode],
        queryFn: () => obtenerEvento(eventCode),
        staleTime: 30 * 1000,
        enabled: eventCode !== '',
    })
}

export function useEventosPorArtista(stageName: string | undefined) {
    return useQuery({
        queryKey: ['eventos', 'artista', stageName],
        queryFn: () => obtenerEventosPorArtista(stageName ?? ''),
        staleTime: UN_MINUTO,
        enabled: Boolean(stageName),
    })
}

export function useCrearEvento() {
    const queryClient = useQueryClient()
    return useMutation({
        mutationFn: crearEvento,
        // Se siembra la cache con la respuesta: la pagina de gestion abre sin otra peticion
        onSuccess: (evento) => queryClient.setQueryData(['evento', evento.eventCode], evento),
    })
}

export function usePublicarEvento() {
    const queryClient = useQueryClient()
    return useMutation({
        mutationFn: publicarEvento,
        onSuccess: (evento) => {
            queryClient.invalidateQueries({ queryKey: ['evento', evento.eventCode] })
            queryClient.invalidateQueries({ queryKey: ['eventos', 'publicados'] })
        },
    })
}

export function useAsociarArtista() {
    const queryClient = useQueryClient()
    return useMutation({
        mutationFn: ({
                         eventCode,
                         artistId,
                     }: {
            eventCode: string
            artistId: number
            stageName: string
        }) => asociarArtista(eventCode, artistId),
        onSuccess: (evento, { stageName }) => {
            queryClient.invalidateQueries({ queryKey: ['evento', evento.eventCode] })
            queryClient.invalidateQueries({ queryKey: ['eventos', 'artista', stageName] })
        },
    })
}
