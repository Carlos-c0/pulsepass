import Aviso from '../../../componentes/Aviso'
import Boton from '../../../componentes/Boton'
import CodigoTexto from '../../../componentes/CodigoTexto'
import Modal from '../../../componentes/Modal'
import { useAvisos } from '../../../componentes/Avisos'
import { mensajeDeError } from '../../../lib/errores'
import { useCancelarTicket } from '../hooks'
import type { Ticket } from '../tipos'

interface ModalConfirmarCancelacionProps {
    ticket: Ticket | null
    onCerrar: () => void
}

export default function ModalConfirmarCancelacion({
                                                      ticket,
                                                      onCerrar,
                                                  }: ModalConfirmarCancelacionProps) {
    const cancelar = useCancelarTicket()
    const { notificar } = useAvisos()

    const cerrar = () => {
        cancelar.reset()
        onCerrar()
    }

    const confirmar = () => {
        if (!ticket) return
        cancelar.mutate(ticket.ticketCode, {
            onSuccess: () => {
                notificar('exito', 'Ticket cancelado.')
                cerrar()
            },
        })
    }

    return (
        <Modal
            abierto={ticket !== null}
            titulo="Cancelar ticket"
            onCerrar={cerrar}
            acciones={
                <>
                    <Boton variante="secundario" onClick={cerrar}>
                        Volver
                    </Boton>
                    <Boton variante="peligro" cargando={cancelar.isPending} onClick={confirmar}>
                        Cancelar ticket
                    </Boton>
                </>
            }
        >
            {ticket && (
                <p className="leading-relaxed">
                    ¿Quieres cancelar el ticket{' '}
                    <strong>
                        <CodigoTexto>{ticket.ticketCode}</CodigoTexto>
                    </strong>{' '}
                    ({ticket.type}) de {ticket.eventName}?
                </p>
            )}
            {/* Un 409 se muestra aqui mismo, sin cerrar el modal (DES-10) */}
            {cancelar.isError && <Aviso tono="error">{mensajeDeError(cancelar.error)}</Aviso>}
        </Modal>
    )
}
