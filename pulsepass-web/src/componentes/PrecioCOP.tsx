import { formatearPrecio } from '../lib/formato'

export default function PrecioCOP({ valor }: { valor: number }) {
    return <span className="tabular-nums">{formatearPrecio(valor)}</span>
}
