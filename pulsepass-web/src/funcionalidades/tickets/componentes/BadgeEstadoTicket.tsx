import Badge, { type Tono } from '../../../componentes/Badge'
import type { EstadoTicket } from '../tipos'

const TONO: Record<EstadoTicket, Tono> = {
    RESERVED: 'celeste',
    PAID: 'verde',
    USED: 'grisOscuro',
    CANCELLED: 'rojo',
}

export default function BadgeEstadoTicket({ estado }: { estado: EstadoTicket }) {
    return <Badge tono={TONO[estado]}>{estado}</Badge>
}
