import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router'
import { RUTAS } from '../../lib/rutas'
import { useAuth } from './AuthContext'
import type { LoginForm } from './tipos'

export function useLogin() {
    const { iniciarSesion } = useAuth()
    return useMutation({
        mutationFn: ({ identifier, password }: LoginForm) => iniciarSesion(identifier, password),
    })
}

// Primero se sale de la pagina protegida y despues se cierra la sesion; al reves,
// RutaProtegida alcanzaria a mandar a /login antes de llegar al inicio
export function useSalir() {
    const { cerrarSesion } = useAuth()
    const navigate = useNavigate()
    return async () => {
        await navigate(RUTAS.inicio)
        cerrarSesion()
    }
}
