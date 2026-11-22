import clsx from 'clsx'
import { Link } from 'react-router'
import { IconoFlechaDerecha } from './Iconos'

// "Ver todos →", "Tickets pagados →"
export default function EnlaceSeccion({
                                          a,
                                          children,
                                          className,
                                      }: {
    a: string
    children: string
    className?: string
}) {
    return (
        <Link
            to={a}
            className={clsx(
                'text-marca-700 inline-flex items-center gap-2 text-[15px] font-medium hover:underline dark:text-indigo-300',
                className,
            )}
        >
            {children}
            <IconoFlechaDerecha className="size-4" />
        </Link>
    )
}
