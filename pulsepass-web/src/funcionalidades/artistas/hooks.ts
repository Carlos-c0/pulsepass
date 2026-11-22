import { useQuery } from '@tanstack/react-query'
import { obtenerArtista, obtenerArtistaPorNombre, obtenerArtistasActivos } from './api'

const CINCO_MINUTOS = 5 * 60 * 1000

export function useArtistasActivos() {
    return useQuery({
        queryKey: ['artistas', 'activos'],
        queryFn: obtenerArtistasActivos,
        staleTime: CINCO_MINUTOS,
    })
}

export function useArtista(id: number) {
    return useQuery({
        queryKey: ['artista', id],
        queryFn: () => obtenerArtista(id),
        staleTime: CINCO_MINUTOS,
        enabled: Number.isInteger(id) && id > 0,
    })
}

export function useArtistaPorNombre(stageName: string) {
    return useQuery({
        queryKey: ['artista', 'nombre', stageName],
        queryFn: () => obtenerArtistaPorNombre(stageName),
        staleTime: CINCO_MINUTOS,
        enabled: stageName !== '',
    })
}
