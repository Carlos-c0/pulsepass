import { z } from 'zod'

// location.state viene de afuera del componente: se valida igual que una respuesta de la API (FE-002)
const EstadoNavegacionSchema = z.object({
    desde: z.string().startsWith('/').optional(),
    motivo: z.literal('expirada').optional(),
    registrado: z.boolean().optional(),
})

export type EstadoNavegacion = z.infer<typeof EstadoNavegacionSchema>

export function leerEstadoNavegacion(state: unknown): EstadoNavegacion {
    const leido = EstadoNavegacionSchema.safeParse(state)
    return leido.success ? leido.data : {}
}
