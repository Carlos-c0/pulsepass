import { z } from 'zod'

export const VenueResponseSchema = z.object({
    code: z.string(),
    name: z.string(),
    city: z.string(),
    address: z.string().nullish(),
    capacity: z.number(),
    active: z.boolean(),
})

export type Sede = z.infer<typeof VenueResponseSchema>
