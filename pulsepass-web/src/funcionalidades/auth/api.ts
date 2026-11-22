import { pedir } from '../../lib/http'
import { AuthResponseSchema, UsuarioAutenticadoSchema, type LoginForm } from './tipos'

export const login = (datos: LoginForm) =>
    pedir('/auth/login', AuthResponseSchema, { metodo: 'POST', cuerpo: datos })

export const obtenerUsuarioActual = () => pedir('/auth/me', UsuarioAutenticadoSchema)
