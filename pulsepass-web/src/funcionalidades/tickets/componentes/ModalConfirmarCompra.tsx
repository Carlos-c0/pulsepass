import { useNavigate } from 'react-router'
import Aviso from '../../../componentes/Aviso'
import Boton from '../../../componentes/Boton'
import { Dato, ListaDatos } from '../../../componentes/ListaDatos'
import Modal from '../../../componentes/Modal'
import { mensajeDeError } from '../../../lib/errores'
import { RUTAS } from '../../../lib/rutas'
import { useComprarTicket } from '../hooks'
import type { TipoTicket } from '../tipos'

interface ModalConfirmarCompraProps {
    abierto: boolean
    onCerrar: () => void
    eventCode: string
    nombreEvento: string
    tipo: TipoTicket
    email: string
}

export default function ModalConfirmarCompra({
                                                 abierto,
                                                 onCerrar,
                                                 eventCode,
                                                 nombreEvento,
                                                 tipo,
                                                 email,
                                             }: ModalConfirmarCompraProps) {
    const comprar = useComprarTicket()
    const navigate = useNavigate()

    const cerrar = () => {
        comprar.reset()
        onCerrar()
    }

    const confirmar = () => {
        comprar.mutate(
            { eventCode, type: tipo },
            { onSuccess: (ticket) => navigate(RUTAS.confirmacion(ticket.ticketCode)) },
        )
    }

    return (
        <Modal
            abierto={abierto}
            titulo="Confirmar compra"
            onCerrar={cerrar}
            acciones={
                <>
                    <Boton variante="secundario" onClick={cerrar}>
                        Cancelar
                    </Boton>
                    <Boton cargando={comprar.isPending} onClick={confirmar}>
                        Confirmar
                    </Boton>
                </>
            }
        >
            <div className="border-b border-slate-200 dark:border-slate-800">
                <ListaDatos>
                    <Dato etiqueta="Evento">{nombreEvento}</Dato>
                    <Dato etiqueta="Tipo">{tipo}</Dato>
                    <Dato etiqueta="Cuenta">{email}</Dato>
                </ListaDatos>
            </div>
            {/* Edad minima, capacidad, estado: lo decide el backend y aqui solo se muestra (409, DES-10) */}
            {comprar.isError && <Aviso tono="error">{mensajeDeError(comprar.error)}</Aviso>}
        </Modal>
    )
}
