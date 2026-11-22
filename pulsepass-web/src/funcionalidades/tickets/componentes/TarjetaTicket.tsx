import Boton from '../../../componentes/Boton'
import CodigoTexto from '../../../componentes/CodigoTexto'
import EnlaceBoton from '../../../componentes/EnlaceBoton'
import PrecioCOP from '../../../componentes/PrecioCOP'
import { formatearFechaCorta } from '../../../lib/formato'
import { RUTAS } from '../../../lib/rutas'
import type { Ticket } from '../tipos'
import BadgeEstadoTicket from './BadgeEstadoTicket'

interface TarjetaTicketProps {
    ticket: Ticket
    onCancelar: (ticket: Ticket) => void
}

export default function TarjetaTicket({ ticket, onCancelar }: TarjetaTicketProps) {
    return (
        <article className="rounded-2xl border border-slate-200 bg-white px-5 py-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
            <div className="flex items-start justify-between gap-3">
                <h3 className="text-lg font-semibold">{ticket.eventName}</h3>
                <BadgeEstadoTicket estado={ticket.status} />
            </div>
            <p className="mt-1.5 text-[15px] text-slate-600 dark:text-slate-400">
                <CodigoTexto className="text-slate-900 dark:text-slate-100">
                    {ticket.ticketCode}
                </CodigoTexto>
                {' · '}
                {ticket.type} · <PrecioCOP valor={ticket.price} /> ·{' '}
                {formatearFechaCorta(ticket.purchaseDate)}
            </p>
            <div className="mt-4 flex flex-wrap justify-end gap-2.5">
                <EnlaceBoton to={RUTAS.ticket(ticket.ticketCode)} variante="secundario">
                    Ver detalle
                </EnlaceBoton>
                {/* Solo se ofrece para PAID; si igual falla, el backend responde 409 */}
                {ticket.status === 'PAID' && (
                    <Boton variante="peligro" onClick={() => onCancelar(ticket)}>
                        Cancelar
                    </Boton>
                )}
            </div>
        </article>
    )
}
