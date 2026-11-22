import { z } from 'zod'
import { ArtistResponseSchema } from '../artistas/tipos'

export const EventCategorySchema = z.enum([
    'MUSIC',
    'SPORTS',
    'TECHNOLOGY',
    'EDUCATION',
    'CULTURE',
    'ENTERTAINMENT',
])
export const EventStatusSchema = z.enum(['DRAFT', 'PUBLISHED', 'SOLD_OUT', 'CANCELLED', 'FINISHED'])

export type CategoriaEvento = z.infer<typeof EventCategorySchema>
export type EstadoEvento = z.infer<typeof EventStatusSchema>

export const EventResponseSchema = z.object({
    eventCode: z.string(),
    name: z.string(),
    description: z.string().nullish(),
    category: EventCategorySchema,
    status: EventStatusSchema,
    eventDate: z.string(),
    minimumAge: z.number(),
    venueCode: z.string(),
    venueName: z.string(),
    artists: z.array(ArtistResponseSchema),
})

export const EventSummaryResponseSchema = z.object({
    eventCode: z.string(),
    name: z.string(),
    category: EventCategorySchema,
    status: EventStatusSchema,
    eventDate: z.string(),
    venueName: z.string(),
    venueCity: z.string(),
})

export type Evento = z.infer<typeof EventResponseSchema>
export type ResumenEvento = z.infer<typeof EventSummaryResponseSchema>

// Formulario de "Crear evento" (solo validacion estructural; las reglas las decide el backend)
export const CrearEventoSchema = z.object({
    eventCode: z
        .string()
        .trim()
        .min(1, 'Ingresa el código del evento')
        .max(50, 'Máximo 50 caracteres'),
    name: z.string().trim().min(1, 'Ingresa el nombre').max(150, 'Máximo 150 caracteres'),
    description: z.string().max(2000, 'Máximo 2000 caracteres'),
    category: EventCategorySchema,
    eventDate: z.string().min(1, 'Elige la fecha y la hora'),
    minimumAge: z
        .number({ error: 'Ingresa la edad mínima' })
        .int('Debe ser un número entero')
        .min(0, 'No puede ser negativa'),
    venueCode: z.string().min(1, 'Elige la sede'),
})

export type CrearEventoForm = z.infer<typeof CrearEventoSchema>
