import clsx from 'clsx'
import { Link } from 'react-router'
import { IconoCalendario, IconoUbicacion } from '../../../componentes/Iconos'
import { formatearFecha } from '../../../lib/formato'
import { RUTAS } from '../../../lib/rutas'
import type { ResumenEvento } from '../tipos'
import BadgeEstadoEvento from './BadgeEstadoEvento'
import { ESTILO_CATEGORIA } from './estilosCategoria'

export default function TarjetaEvento({ evento }: { evento: ResumenEvento }) {
    const { fondo, texto, Icono } = ESTILO_CATEGORIA[evento.category]

    return (
        <Link
            to={RUTAS.evento(evento.eventCode)}
            className="group focus-visible:outline-marca-600 block h-full overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm transition hover:shadow-md focus-visible:outline-2 focus-visible:outline-offset-2 dark:border-slate-800 dark:bg-slate-900"
        >
            <div className={clsx('flex h-36 items-center justify-center', fondo, texto)}>
                <Icono className="size-12" strokeWidth={1.6} />
            </div>
            <div className="p-4">
                <p className={clsx('text-xs font-bold tracking-wider', texto)}>{evento.category}</p>
                <h3 className="group-hover:text-marca-700 mt-1.5 text-lg leading-snug font-semibold dark:group-hover:text-indigo-300">
                    {evento.name}
                </h3>
                <p className="mt-1.5 flex items-center gap-2 text-[15px] text-slate-600 dark:text-slate-400">
                    <IconoCalendario className="size-4" />
                    {formatearFecha(evento.eventDate)}
                </p>
                <p className="mt-1 flex items-center gap-2 text-[15px] text-slate-600 dark:text-slate-400">
                    <IconoUbicacion className="size-4" />
                    {evento.venueCity}
                </p>
                <div className="mt-3">
                    <BadgeEstadoEvento estado={evento.status} />
                </div>
            </div>
        </Link>
    )
}
