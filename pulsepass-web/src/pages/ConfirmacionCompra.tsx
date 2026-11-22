import { useParams } from 'react-router'
import Cargando from '../componentes/Cargando'
import CodigoTexto from '../componentes/CodigoTexto'
import Contenedor from '../componentes/Contenedor'
import EnlaceBoton from '../componentes/EnlaceBoton'
import EstadoError from '../componentes/EstadoError'
import { IconoCheck } from '../componentes/Iconos'
import PrecioCOP from '../componentes/PrecioCOP'
import Tarjeta from '../componentes/Tarjeta'
import BadgeEstadoTicket from '../funcionalidades/tickets/componentes/BadgeEstadoTicket'
import { useTicket } from '../funcionalidades/tickets/hooks'
import { esEstado } from '../lib/errores'
import { RUTAS } from '../lib/rutas'
import NoEncontrada from './NoEncontrada'

export default function ConfirmacionCompra() {
    const { ticketCode = '' } = useParams()
    const ticket = useTicket(ticketCode)

    if (esEstado(ticket.error, 403, 404)) return <NoEncontrada titulo="Ticket no disponible" />

    return (
        <Contenedor className="max-w-[33rem] sm:pt-8">
            {ticket.isPending && <Cargando filas={1} />}
            {ticket.isError && <EstadoError error={ticket.error} onReintentar={() => ticket.refetch()} />}
            {ticket.data && (
                <Tarjeta className="flex flex-col items-center px-8 py-9 text-center">
          <span className="grid size-18 place-items-center rounded-full bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-300">
            <IconoCheck className="size-8" strokeWidth={2.4} />
          </span>
                    <h1 className="mt-6 text-[1.6rem] leading-tight font-bold">
                        ¡Compra realizada con éxito!
                    </h1>
                    <p className="font-titulo mt-5 text-lg font-semibold">{ticket.data.eventName}</p>
                    <p className="mt-1.5 text-slate-600 dark:text-slate-400">
                        Ticket{' '}
                        <CodigoTexto className="text-slate-900 dark:text-slate-100">
                            {ticket.data.ticketCode}
                        </CodigoTexto>{' '}
                        · {ticket.data.type} · <PrecioCOP valor={ticket.data.price} />
                    </p>
                    <div className="mt-2">
                        <BadgeEstadoTicket estado={ticket.data.status} />
                    </div>
                    <div className="mt-6 flex flex-wrap justify-center gap-3">
                        <EnlaceBoton to={RUTAS.misTickets}>Ver mis tickets</EnlaceBoton>
                        <EnlaceBoton to={RUTAS.eventos} variante="secundario">
                            Seguir explorando
                        </EnlaceBoton>
                    </div>
                </Tarjeta>
            )}
        </Contenedor>
    )
}
