import clsx from 'clsx'
import { Link } from 'react-router'
import { RUTAS } from '../../../lib/rutas'
import type { Artista } from '../tipos'

export type TamanoAvatar = 'sm' | 'md' | 'lg' | 'xl'

const TAMANO: Record<TamanoAvatar, string> = {
    sm: 'size-11 text-sm',
    md: 'size-16 text-xl',
    lg: 'size-24 text-3xl',
    xl: 'size-32 text-4xl',
}

// Cada artista conserva su color segun su id (SB indigo, NW verde, CS celeste…)
const COLORES = [
    'bg-indigo-100 text-indigo-800 dark:bg-indigo-950 dark:text-indigo-300',
    'bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-300',
    'bg-sky-100 text-sky-800 dark:bg-sky-950 dark:text-sky-300',
    'bg-rose-100 text-rose-800 dark:bg-rose-950 dark:text-rose-300',
    'bg-amber-100 text-amber-800 dark:bg-amber-950 dark:text-amber-300',
    'bg-violet-100 text-violet-800 dark:bg-violet-950 dark:text-violet-300',
] as const

function colorDe(id: number): string {
    return COLORES[(id - 1 + COLORES.length) % COLORES.length] ?? COLORES[0]
}

function iniciales(nombre: string): string {
    return nombre
        .split(/\s+/)
        .filter(Boolean)
        .slice(0, 2)
        .map((p) => p.charAt(0))
        .join('')
        .toUpperCase()
}

// Sin fotos en la API: un circulo con las iniciales del nombre artistico
export function Iniciales({
                              nombre,
                              id,
                              tamano = 'sm',
                          }: {
    nombre: string
    id: number
    tamano?: TamanoAvatar
}) {
    return (
        <span
            aria-hidden="true"
            className={clsx(
                'font-titulo grid shrink-0 place-items-center rounded-full font-semibold',
                TAMANO[tamano],
                colorDe(id),
            )}
        >
      {iniciales(nombre)}
    </span>
    )
}

interface AvatarArtistaProps {
    artista: Artista
    tamano?: TamanoAvatar
    // En movil el detalle de evento muestra solo los circulos
    ocultarNombreEnMovil?: boolean
}

export default function AvatarArtista({
                                          artista,
                                          tamano = 'sm',
                                          ocultarNombreEnMovil = false,
                                      }: AvatarArtistaProps) {
    return (
        <Link
            to={RUTAS.artista(artista.id)}
            className="focus-visible:outline-marca-600 flex items-center gap-3 rounded-full focus-visible:outline-2 focus-visible:outline-offset-2"
        >
            <Iniciales nombre={artista.stageName} id={artista.id} tamano={tamano} />
            <span className={clsx('font-medium', ocultarNombreEnMovil && 'sr-only sm:not-sr-only')}>
        {artista.stageName}
      </span>
        </Link>
    )
}
