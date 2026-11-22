import Badge, { type Tono } from '../../../componentes/Badge'
import type { EstadoEvento } from '../tipos'

const TONO: Record<EstadoEvento, Tono> = {
    DRAFT: 'gris',
    PUBLISHED: 'verde',
    SOLD_OUT: 'ambar',
    CANCELLED: 'rojo',
    FINISHED: 'grisOscuro',
}

export default function BadgeEstadoEvento({ estado }: { estado: EstadoEvento }) {
    return <Badge tono={TONO[estado]}>{estado}</Badge>
}
