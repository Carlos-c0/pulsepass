import { useState } from 'react'
import { useParams } from 'react-router'
import Boton from '../componentes/Boton'
import Cargando from '../componentes/Cargando'
import CodigoTexto from '../componentes/CodigoTexto'
import Contenedor from '../componentes/Contenedor'
import EnlaceVolver from '../componentes/EnlaceVolver'
import EstadoError from '../componentes/EstadoError'
import { IconoCheck, IconoCopiar } from '../componentes/Iconos'
import { Dato, ListaDatos } from '../componentes/ListaDatos'
import PrecioCOP from '../componentes/PrecioCOP'
import Tarjeta from '../componentes/Tarjeta'
import BadgeEstadoTicket from '../funcionalidades/tickets/componentes/BadgeEstadoTicket'
import ModalConfirmarCancelacion from '../funcionalidades/tickets/componentes/ModalConfirmarCancelacion'
import { useTicket } from '../funcionalidades/tickets/hooks'
import { esEstado } from '../lib/errores'
import { formatearMomento } from '../lib/formato'
import { RUTAS } from '../lib/rutas'
import NoEncontrada from './NoEncontrada'

export default function DetalleTicket() {
    const { ticketCode = '' } = useParams()
    const ticket = useTicket(ticketCode)
    const [cancelando, setCancelando] = useState(false)
    const [copiado, setCopiado] = useState(false)

    // Ajeno o inexistente se ven igual: no se revela que el ticket existe
    if (esEstado(ticket.error, 403, 404)) return <NoEncontrada titulo="Ticket no disponible" />

    const copiar = async (codigo: string) => {
        try {
            await navigator.clipboard.writeText(codigo)
            setCopiado(true)
        } catch {
            setCopiado(false)
        }
    }

    return (
        <Contenedor estrecho className="space-y-6">
            <EnlaceVolver a={RUTAS.misTickets}>Mis tickets</EnlaceVolver>
            {ticket.isPending && <Cargando filas={1} />}
            {ticket.isError && <EstadoError error={ticket.error} onReintentar={() => ticket.refetch()} />}
            {ticket.data && (
                <>
                    <Tarjeta className="px-7">
                        <div className="flex items-start justify-between gap-3">
                            <h1 className="text-[1.6rem] leading-tight font-bold">{ticket.data.eventName}</h1>
                            <BadgeEstadoTicket estado={ticket.data.status} />
                        </div>
                        <div className="mt-3 border-b border-slate-200 dark:border-slate-800">
                            <ListaDatos>
                                <Dato etiqueta="Código">
                  <span className="flex items-center gap-3">
                    <CodigoTexto>{ticket.data.ticketCode}</CodigoTexto>
                    <Boton
                        variante="secundario"
                        tamano="pequeno"
                        onClick={() => copiar(ticket.data.ticketCode)}
                        className="px-3"
                    >
                      {copiado ? (
                          <IconoCheck className="size-4" />
                      ) : (
                          <IconoCopiar className="size-4" />
                      )}
                        {copiado ? 'Copiado' : 'Copiar'}
                    </Boton>
                  </span>
                                </Dato>
                                <Dato etiqueta="Tipo">{ticket.data.type}</Dato>
                                <Dato etiqueta="Precio">
                                    <PrecioCOP valor={ticket.data.price} />
                                </Dato>
                                <Dato etiqueta="Comprado el">{formatearMomento(ticket.data.purchaseDate)}</Dato>
                                {/* TicketResponse solo trae el correo del titular */}
                                <Dato etiqueta="Titular">{ticket.data.userEmail}</Dato>
                            </ListaDatos>
                        </div>
                        <p className="mt-4 text-[15px] text-slate-600 dark:text-slate-400">
                            Presenta este código en el ingreso.
                        </p>
                    </Tarjeta>
                    {ticket.data.status === 'PAID' && (
                        <div className="flex justify-end">
                            <Boton variante="peligro" onClick={() => setCancelando(true)}>
                                Cancelar ticket
                            </Boton>
                        </div>
                    )}
                    <ModalConfirmarCancelacion
                        ticket={cancelando ? ticket.data : null}
                        onCerrar={() => setCancelando(false)}
                    />
                </>
            )}
        </Contenedor>
    )
}
