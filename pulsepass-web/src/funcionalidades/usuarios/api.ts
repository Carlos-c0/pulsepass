import { pedir } from '../../lib/http'
import { UserResponseSchema, type RegistroForm } from './tipos'

export const registrarUsuario = (datos: RegistroForm) =>
    pedir('/users', UserResponseSchema, { metodo: 'POST', cuerpo: datos })

export const obtenerUsuarioPorEmail = (email: string) =>
    pedir(`/users/by-email?${new URLSearchParams({ email })}`, UserResponseSchema)

export const obtenerUsuarioPorUsername = (username: string) =>
    pedir(`/users/by-username?${new URLSearchParams({ username })}`, UserResponseSchema)
