import { z } from 'zod'
import { pedir } from '../../lib/http'
import { VenueResponseSchema } from './tipos'

export const obtenerSedesActivas = () => pedir('/venues/active', z.array(VenueResponseSchema))

export const obtenerSede = (code: string) =>
    pedir(`/venues/${encodeURIComponent(code)}`, VenueResponseSchema)
