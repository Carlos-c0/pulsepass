import { z } from 'zod'

export const RolSchema = z.enum(['USER', 'STAFF', 'ADMIN'])
export type Rol = z.infer<typeof RolSchema>

export const AuthResponseSchema = z.object({
    accessToken: z.string().min(1),
    tokenType: z.literal('Bearer'),
    expiresIn: z.number().positive(),
})

export const UsuarioAutenticadoSchema = z.object({
    id: z.number(),
    username: z.string(),
    email: z.string(),
    // Un rol que el cliente no conoce se ignora en vez de romper la sesion
    roles: z.array(z.string()).transform((roles) =>
        roles.flatMap((r) => {
            const rol = RolSchema.safeParse(r)
            return rol.success ? [rol.data] : []
        }),
    ),
})

export type UsuarioAutenticado = z.infer<typeof UsuarioAutenticadoSchema>

export type Sesion =
    | { estado: 'cargando' }
    | { estado: 'anonimo'; motivo?: 'expirada' }
    | { estado: 'autenticado'; token: string; expiraEn: number; usuario: UsuarioAutenticado }

export const LoginSchema = z.object({
    identifier: z.string().trim().min(1, 'Ingresa tu usuario o correo'),
    password: z.string().min(1, 'Ingresa tu contraseña'),
})

export type LoginForm = z.infer<typeof LoginSchema>
