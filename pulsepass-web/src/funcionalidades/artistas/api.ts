import { z } from 'zod'
import { pedir } from '../../lib/http'
import { ArtistResponseSchema } from './tipos'

export const obtenerArtistasActivos = () => pedir('/artists/active', z.array(ArtistResponseSchema))

export const obtenerArtista = (id: number) => pedir(`/artists/${id}`, ArtistResponseSchema)

export const obtenerArtistaPorNombre = (stageName: string) =>
    pedir(`/artists/by-stage-name?${new URLSearchParams({ stageName })}`, ArtistResponseSchema)
