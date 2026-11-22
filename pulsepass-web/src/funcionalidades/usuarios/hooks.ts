import { useMutation, useQuery } from '@tanstack/react-query'
import { obtenerUsuarioPorEmail, obtenerUsuarioPorUsername, registrarUsuario } from './api'

const UN_MINUTO = 60 * 1000

export function useRegistrarUsuario() {
    return useMutation({ mutationFn: registrarUsuario })
}

export function useUsuarioPorEmail(email: string) {
    return useQuery({
        queryKey: ['usuario', email],
        queryFn: () => obtenerUsuarioPorEmail(email),
        staleTime: UN_MINUTO,
        enabled: email !== '',
    })
}

export function useUsuarioPorUsername(username: string) {
    return useQuery({
        queryKey: ['usuario', 'username', username],
        queryFn: () => obtenerUsuarioPorUsername(username),
        staleTime: UN_MINUTO,
        enabled: username !== '',
    })
}
