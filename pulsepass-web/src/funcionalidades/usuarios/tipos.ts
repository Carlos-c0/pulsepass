import { z } from 'zod'

export const UserResponseSchema = z.object({
    username: z.string(),
    email: z.string(),
    active: z.boolean(),
    firstName: z.string().nullish(),
    lastName: z.string().nullish(),
    phone: z.string().nullish(),
    city: z.string().nullish(),
    birthDate: z.string().nullish(),
})

export type Usuario = z.infer<typeof UserResponseSchema>

const hoy = () => new Date().toISOString().slice(0, 10)

export const RegistroSchema = z.object({
    username: z
        .string()
        .trim()
        .min(1, 'Ingresa un nombre de usuario')
        .max(50, 'Máximo 50 caracteres'),
    email: z.email('Ingresa un correo válido').max(150, 'Máximo 150 caracteres'),
    firstName: z.string().trim().min(1, 'Ingresa tus nombres').max(100, 'Máximo 100 caracteres'),
    lastName: z.string().trim().min(1, 'Ingresa tus apellidos').max(100, 'Máximo 100 caracteres'),
    birthDate: z
        .string()
        .min(1, 'Ingresa tu fecha de nacimiento')
        .refine((valor) => valor <= hoy(), 'La fecha no puede ser futura'),
    password: z
        .string()
        .min(12, 'La contraseña debe tener al menos 12 caracteres')
        // BCrypt solo usa 72 bytes; una tilde o una ñ ocupan dos
        .refine(
            (valor) => new TextEncoder().encode(valor).length <= 72,
            'La contraseña es demasiado larga',
        ),
})

export type RegistroForm = z.infer<typeof RegistroSchema>
