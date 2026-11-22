import { Link } from 'react-router'
import { RUTAS } from '../../../lib/rutas'
import type { Artista } from '../tipos'
import { Iniciales } from './AvatarArtista'

export default function TarjetaArtista({ artista }: { artista: Artista }) {
    return (
        <Link
            to={RUTAS.artista(artista.id)}
            className="focus-visible:outline-marca-600 flex h-full flex-col items-center rounded-2xl border border-slate-200 bg-white px-6 py-6 text-center shadow-sm hover:shadow-md focus-visible:outline-2 focus-visible:outline-offset-2 dark:border-slate-800 dark:bg-slate-900"
        >
            <Iniciales nombre={artista.stageName} id={artista.id} tamano="lg" />
            <span className="font-titulo mt-5 text-lg font-semibold">{artista.stageName}</span>
            {artista.genre && (
                <span className="mt-2 text-sm text-slate-600 dark:text-slate-400">{artista.genre}</span>
            )}
        </Link>
    )
}
