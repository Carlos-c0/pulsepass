import clsx from 'clsx'
import { Link, useParams } from 'react-router'
import Cargando from '../componentes/Cargando'
import Contenedor from '../componentes/Contenedor'
import EnlaceVolver from '../componentes/EnlaceVolver'
import EstadoError from '../componentes/EstadoError'
import { IconoCalendario, IconoSede, IconoUsuario } from '../componentes/Iconos'
import AvatarArtista from '../funcionalidades/artistas/componentes/AvatarArtista'
import BadgeEstadoEvento from '../funcionalidades/eventos/componentes/BadgeEstadoEvento'
import { ESTILO_CATEGORIA } from '../funcionalidades/eventos/componentes/estilosCategoria'
import { useEvento } from '../funcionalidades/eventos/hooks'
import { useSede } from '../funcionalidades/sedes/hooks'
import PanelCompra from '../funcionalidades/tickets/componentes/PanelCompra'
import { esEstado } from '../lib/errores'
import { formatearFechaHora } from '../lib/formato'
import { RUTAS } from '../lib/rutas'
import NoEncontrada from './NoEncontrada'

const capacidad = new Intl.NumberFormat('es-CO')

export default function DetalleEvento() {
    const { eventCode = '' } = useParams()
    const evento = useEvento(eventCode)
    // EventResponse no trae ciudad ni capacidad: se completan con la sede
    const sede = useSede(evento.data?.venueCode)

    if (evento.isPending)
        return (
            <Contenedor>
                <Cargando />
            </Contenedor>
        )
    // Un borrador o un codigo que no existe se ven igual: no se revela nada (GAP-02)
    if (esEstado(evento.error, 403, 404)) return <NoEncontrada titulo="Evento no disponible" />
    if (evento.isError)
        return (
            <Contenedor>
                <EstadoError error={evento.error} onReintentar={() => evento.refetch()} />
            </Contenedor>
        )

    const e = evento.data
    const { fondo, texto, Icono } = ESTILO_CATEGORIA[e.category]

    return (
        <Contenedor className="space-y-5 sm:space-y-6">
            <EnlaceVolver a={RUTAS.eventos}>Volver a la cartelera</EnlaceVolver>

            <div className="grid gap-6 lg:grid-cols-[1fr_23.5rem] lg:items-start">
                <article className="md:overflow-hidden md:rounded-2xl md:border md:border-slate-200 md:bg-white md:shadow-sm dark:md:border-slate-800 dark:md:bg-slate-900">
                    <div
                        className={clsx(
                            'flex h-45 items-center justify-center rounded-2xl md:h-55 md:rounded-none',
                            fondo,
                            texto,
                        )}
                    >
                        <Icono className="size-18" strokeWidth={1.5} />
                    </div>
                    <div className="space-y-5 pt-5 md:p-6">
                        <div className="space-y-3">
                            <p className="flex items-center gap-2.5">
                <span className={clsx('text-[13px] font-bold tracking-wider', texto)}>
                  {e.category}
                </span>
                                <BadgeEstadoEvento estado={e.status} />
                            </p>
                            <h1 className="text-[1.6rem] leading-tight font-bold sm:text-[2rem]">{e.name}</h1>
                            <div className="flex flex-col gap-3 text-[15px] text-slate-600 sm:flex-row sm:gap-5 dark:text-slate-400">
                                <p className="flex items-center gap-2">
                                    <IconoCalendario className="size-4" /> {formatearFechaHora(e.eventDate)}
                                </p>
                                <p className="flex items-center gap-2">
                                    <IconoUsuario className="size-4" /> Edad mínima: {e.minimumAge}
                                </p>
                            </div>
                        </div>
                        {e.description && <p className="leading-relaxed">{e.description}</p>}

                        <section className="space-y-3">
                            <h2 className="text-lg font-semibold">Artistas</h2>
                            {e.artists.length === 0 ? (
                                <p className="text-sm text-slate-600 dark:text-slate-400">
                                    Aún no hay artistas confirmados.
                                </p>
                            ) : (
                                <ul className="flex flex-wrap gap-4 sm:gap-6">
                                    {e.artists.map((a) => (
                                        <li key={a.id}>
                                            <AvatarArtista artista={a} ocultarNombreEnMovil />
                                        </li>
                                    ))}
                                </ul>
                            )}
                        </section>

                        <section className="space-y-3">
                            <h2 className="text-lg font-semibold">Sede</h2>
                            <Link
                                to={RUTAS.sede(e.venueCode)}
                                className="focus-visible:outline-marca-600 flex items-center gap-4 rounded-xl border border-slate-200 bg-white px-4 py-3.5 hover:bg-slate-50 focus-visible:outline-2 dark:border-slate-800 dark:bg-slate-900 dark:hover:bg-slate-800"
                            >
                                <IconoSede className="text-marca-700 hidden size-6 shrink-0 sm:block dark:text-indigo-300" />
                                <span>
                  <span className="block font-semibold">{e.venueName}</span>
                                    {sede.data && (
                                        <span className="block text-sm text-slate-600 dark:text-slate-400">
                      {sede.data.city} · Capacidad {capacidad.format(sede.data.capacity)}
                    </span>
                                    )}
                </span>
                            </Link>
                        </section>
                    </div>
                </article>

                <PanelCompra evento={e} />
            </div>
        </Contenedor>
    )
}
