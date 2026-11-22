import { useState } from 'react'
import Cargando from '../componentes/Cargando'
import Contenedor, { TituloPagina } from '../componentes/Contenedor'
import EnlaceBoton from '../componentes/EnlaceBoton'
import EstadoError from '../componentes/EstadoError'
import EstadoVacio from '../componentes/EstadoVacio'
import FiltroChips from '../componentes/FiltroChips'
import ModalConfirmarCancelacion from '../funcionalidades/tickets/componentes/ModalConfirmarCancelacion'
import TarjetaTicket from '../funcionalidades/tickets/componentes/TarjetaTicket'
import { useMisTickets } from '../funcionalidades/tickets/hooks'
import type { EstadoTicket, Ticket } from '../funcionalidades/tickets/tipos'
import { RUTAS } from '../lib/rutas'

type Filtro = 'TODOS' | Exclude<EstadoTicket, 'RESERVED'>

const OPCIONES: { valor: Filtro; etiqueta: string }[] = [
    { valor: 'TODOS', etiqueta: 'Todos' },
    { valor: 'PAID', etiqueta: 'Pagados' },
    { valor: 'USED', etiqueta: 'Usados' },
    { valor: 'CANCELLED', etiqueta: 'Cancelados' },
]

export default function MisTickets() {
    const tickets = useMisTickets()
    const [filtro, setFiltro] = useState<Filtro>('TODOS')
    const [aCancelar, setACancelar] = useState<Ticket | null>(null)

    const visibles = (tickets.data ?? []).filter((t) => filtro === 'TODOS' || t.status === filtro)

    return (
        <Contenedor className="space-y-6">
            <TituloPagina>Mis tickets</TituloPagina>
            <FiltroChips
                etiqueta="Filtrar por estado"
                opciones={OPCIONES}
                seleccionado={filtro}
                onCambiar={setFiltro}
            />

            {tickets.isPending && <Cargando />}
            {tickets.isError && (
                <EstadoError error={tickets.error} onReintentar={() => tickets.refetch()} />
            )}
            {tickets.data?.length === 0 && (
                <EstadoVacio
                    mensaje="Aún no tienes tickets"
                    accion={
                        <EnlaceBoton to={RUTAS.eventos} variante="secundario">
                            Ver la cartelera
                        </EnlaceBoton>
                    }
                />
            )}
            {tickets.data && tickets.data.length > 0 && visibles.length === 0 && (
                <EstadoVacio mensaje="No tienes tickets con este estado" />
            )}
            <ul className="space-y-4">
                {visibles.map((t) => (
                    <li key={t.ticketCode}>
                        <TarjetaTicket ticket={t} onCancelar={setACancelar} />
                    </li>
                ))}
            </ul>

            <ModalConfirmarCancelacion ticket={aCancelar} onCerrar={() => setACancelar(null)} />
        </Contenedor>
    )
}
