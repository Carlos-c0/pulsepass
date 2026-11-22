import { Link } from 'react-router'
import { IconoFlechaIzquierda } from './Iconos'

export default function EnlaceVolver({ a, children }: { a: string; children: string }) {
    return (
        <Link
            to={a}
            className="text-marca-700 inline-flex items-center gap-2 text-[15px] font-medium hover:underline dark:text-indigo-300"
        >
            <IconoFlechaIzquierda className="size-4" />
            {children}
        </Link>
    )
}
