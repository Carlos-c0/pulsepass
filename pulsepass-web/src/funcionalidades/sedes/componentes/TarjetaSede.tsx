import { Link } from 'react-router'
import Badge from '../../../componentes/Badge'
import { IconoUbicacion } from '../../../componentes/Iconos'
import { RUTAS } from '../../../lib/rutas'
import type { Sede } from '../tipos'

const capacidad = new Intl.NumberFormat('es-CO')

export default function TarjetaSede({ sede }: { sede: Sede }) {
    return (
        <Link
            to={RUTAS.sede(sede.code)}
            className="focus-visible:outline-marca-600 block h-full rounded-2xl border border-slate-200 bg-white px-5 py-5 shadow-sm hover:shadow-md focus-visible:outline-2 focus-visible:outline-offset-2 dark:border-slate-800 dark:bg-slate-900"
        >
            <div className="flex items-start justify-between gap-3">
                <h3 className="text-lg leading-snug font-semibold">{sede.name}</h3>
                <Badge tono={sede.active ? 'verde' : 'gris'}>{sede.active ? 'Activa' : 'Inactiva'}</Badge>
            </div>
            <p className="mt-2 flex items-center gap-2 text-[15px] text-slate-600 dark:text-slate-400">
                <IconoUbicacion className="size-4" /> {sede.city} · Cap. {capacidad.format(sede.capacity)}
            </p>
        </Link>
    )
}
