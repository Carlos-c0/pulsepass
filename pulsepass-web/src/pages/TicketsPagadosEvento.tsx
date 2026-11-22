import { useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import Aviso from '../componentes/Aviso'
import { useAvisos } from '../componentes/Avisos'
import Boton from '../componentes/Boton'
import Campo from '../componentes/Campo'
import Cargando from '../componentes/Cargando'
import CodigoTexto from '../componentes/CodigoTexto'
import Contenedor, { TituloPagina } from '../componentes/Contenedor'
import EstadoError from '../componentes/EstadoError'
import EstadoVacio from '../componentes/EstadoVacio'
import PrecioCOP from '../componentes/PrecioCOP'
import Tabla, { type Columna } from '../componentes/Tabla'
import BadgeEstadoTicket from '../funcionalidades/tickets/componentes/BadgeEstadoTicket'
import { useTicketsPagadosPorEvento, useUsarTicket } from '../funcionalidades/tickets/hooks'
import type { Ticket } from '../funcionalidades/tickets/tipos'
import { esEstado, mensajeDeError } from '../lib/errores'
import { RUTAS } from '../lib/rutas'

export default function TicketsPagadosEvento() {
    // Sin codigo se llega desde la navbar: primero hay que elegir el evento
    const { eventCode = '' } = useParams()
    const navigate = useNavigate()
    const [otroEvento, setOtroEvento] = useState(eventCode)
    const tickets = useTicketsPagadosPorEvento(eventCode)
    const usar = useUsarTicket()
    const { notificar } = useAvisos()

    const marcar = (ticketCode: string) =>
        usar.mutate(ticketCode, {
            onSuccess: () => notificar('exito', `Ingreso registrado: ${ticketCode}`),
        })

    const botonUsar = (t: Ticket, ancho = false) => (
        <Boton
            tamano={ancho ? 'normal' : 'pequeno'}
            onClick={() => marcar(t.ticketCode)}
            disabled={usar.isPending}
            className={ancho ? 'w-full' : 'min-w-19'}
        >
            Usar
        </Boton>
    )

    // TicketResponse solo trae el correo del titular
    const columnas: Columna<Ticket>[] = [
        { encabezado: 'Código', celda: (t) => <CodigoTexto>{t.ticketCode}</CodigoTexto> },
        { encabezado: 'Tipo', celda: (t) => t.type },
        { encabezado: 'Titular', celda: (t) => t.userEmail },
        { encabezado: 'Precio', celda: (t) => <PrecioCOP valor={t.price} /> },
        { encabezado: 'Estado', celda: (t) => <BadgeEstadoTicket estado={t.status} /> },
        { encabezado: 'Acción', celda: (t) => botonUsar(t) },
    ]

    return (
        <Contenedor className="space-y-5">
            <div className="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
                <TituloPagina className="md:pb-2">
                    Tickets pagados
                    {eventCode && <> · {eventCode}</>}
                </TituloPagina>
                <form
                    className="flex items-end gap-2.5"
                    onSubmit={(e) => {
                        e.preventDefault()
                        if (otroEvento.trim()) navigate(RUTAS.ticketsPagados(otroEvento.trim().toUpperCase()))
                    }}
                >
                    <Campo
                        etiqueta="Evento"
                        value={otroEvento}
                        placeholder="CMF-2026"
                        className="flex-1 md:w-45"
                        onChange={(e) => setOtroEvento(e.target.value)}
                    />
                    <Boton type="submit" variante="secundario">
                        Ir
                    </Boton>
                </form>
            </div>

            {!eventCode && (
                <EstadoVacio mensaje="Escribe el código de un evento para ver sus tickets pagados" />
            )}
            {usar.isError && <Aviso tono="advertencia">{mensajeDeError(usar.error)}</Aviso>}
            {eventCode && tickets.isPending && <Cargando />}
            {esEstado(tickets.error, 404) && (
                <Aviso tono="error">No existe un evento con ese código.</Aviso>
            )}
            {tickets.isError && !esEstado(tickets.error, 404) && (
                <EstadoError error={tickets.error} onReintentar={() => tickets.refetch()} />
            )}
            {tickets.data && (
                <>
                    <p className="text-[15px] text-slate-600 dark:text-slate-400">
                        Total: {tickets.data.length}
                    </p>
                    {tickets.data.length === 0 ? (
                        <EstadoVacio mensaje="No hay tickets pagados" />
                    ) : (
                        <Tabla
                            columnas={columnas}
                            filas={tickets.data}
                            clave={(t) => t.ticketCode}
                            tarjeta={(t) => (
                                <div className="space-y-3">
                                    <div className="flex items-center justify-between gap-3">
                                        <CodigoTexto className="font-bold">{t.ticketCode}</CodigoTexto>
                                        <BadgeEstadoTicket estado={t.status} />
                                    </div>
                                    <p className="text-[15px] text-slate-600 dark:text-slate-400">
                                        {t.type} · {t.userEmail} · <PrecioCOP valor={t.price} />
                                    </p>
                                    {botonUsar(t, true)}
                                </div>
                            )}
                        />
                    )}
                </>
            )}
        </Contenedor>
    )
}
