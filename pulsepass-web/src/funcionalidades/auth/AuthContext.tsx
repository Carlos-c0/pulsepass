import { useQueryClient } from '@tanstack/react-query'
import {
    createContext,
    useCallback,
    useContext,
    useEffect,
    useMemo,
    useState,
    type ReactNode,
} from 'react'
import { registrarAlNoAutorizado } from '../../lib/http'
import { borrarToken, guardarToken, restaurarToken } from '../../lib/sesionToken'
import { login, obtenerUsuarioActual } from './api'
import type { Rol, Sesion } from './tipos'

interface AuthContextValue {
    sesion: Sesion
    iniciarSesion: (identifier: string, password: string) => Promise<void>
    cerrarSesion: () => void
    tieneRol: (...roles: Rol[]) => boolean
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
    const queryClient = useQueryClient()
    // Si al arrancar hay un token vigente, se arranca "cargando" mientras se pide /me
    const [guardadoAlIniciar] = useState(() => restaurarToken())
    const [sesion, setSesion] = useState<Sesion>(
        guardadoAlIniciar ? { estado: 'cargando' } : { estado: 'anonimo' },
    )

    const terminar = useCallback(
        (motivo?: 'expirada') => {
            borrarToken()
            queryClient.clear()
            setSesion(motivo ? { estado: 'anonimo', motivo } : { estado: 'anonimo' })
        },
        [queryClient],
    )

    // Cualquier 401 con token cierra la sesion local; RutaProtegida lleva a /login
    useEffect(() => {
        registrarAlNoAutorizado(() => terminar('expirada'))
        return () => registrarAlNoAutorizado(null)
    }, [terminar])

    // Restaurar la sesion de la pestana despues de recargar
    useEffect(() => {
        if (!guardadoAlIniciar) return
        let vigente = true
        obtenerUsuarioActual()
            .then((usuario) => {
                if (vigente) setSesion({ estado: 'autenticado', ...guardadoAlIniciar, usuario })
            })
            .catch(() => {
                if (vigente) terminar()
            })
        return () => {
            vigente = false
        }
    }, [guardadoAlIniciar, terminar])

    // Cierre local cuando vence el token (no hay refresh token, GAP-08)
    useEffect(() => {
        if (sesion.estado !== 'autenticado') return
        const restante = sesion.expiraEn - Date.now()
        const temporizador = setTimeout(() => terminar('expirada'), Math.max(restante, 0))
        return () => clearTimeout(temporizador)
    }, [sesion, terminar])

    const iniciarSesion = useCallback(async (identifier: string, password: string) => {
        const respuesta = await login({ identifier, password })
        const guardado = guardarToken(respuesta.accessToken, respuesta.expiresIn)
        try {
            // Los roles salen de /me; el JWT no se decodifica en el cliente
            const usuario = await obtenerUsuarioActual()
            setSesion({ estado: 'autenticado', ...guardado, usuario })
        } catch (error) {
            borrarToken()
            throw error
        }
    }, [])

    const cerrarSesion = useCallback(() => terminar(), [terminar])

    const tieneRol = useCallback(
        (...roles: Rol[]) =>
            sesion.estado === 'autenticado' && roles.some((r) => sesion.usuario.roles.includes(r)),
        [sesion],
    )

    const valor = useMemo(
        () => ({ sesion, iniciarSesion, cerrarSesion, tieneRol }),
        [sesion, iniciarSesion, cerrarSesion, tieneRol],
    )

    return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
    const contexto = useContext(AuthContext)
    if (!contexto) throw new Error('useAuth debe usarse dentro de AuthProvider')
    return contexto
}
