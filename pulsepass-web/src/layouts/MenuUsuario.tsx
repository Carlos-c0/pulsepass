import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router'
import { IconoChevron } from '../componentes/Iconos'
import { useAuth } from '../funcionalidades/auth/AuthContext'
import { useSalir } from '../funcionalidades/auth/hooks'
import type { UsuarioAutenticado } from '../funcionalidades/auth/tipos'
import { useUsuarioPorEmail } from '../funcionalidades/usuarios/hooks'
import { RUTAS } from '../lib/rutas'

const estiloItem =
    'block w-full px-4 py-2 text-left text-sm hover:bg-slate-100 focus-visible:bg-slate-100 focus-visible:outline-none dark:hover:bg-slate-800 dark:focus-visible:bg-slate-800'

export default function MenuUsuario({ usuario }: { usuario: UsuarioAutenticado }) {
    const { tieneRol } = useAuth()
    const salir = useSalir()
    const [abierto, setAbierto] = useState(false)
    // /api/auth/me no trae nombres: el nombre visible sale del perfil, con el usuario como respaldo
    const perfil = useUsuarioPorEmail(usuario.email)
    const nombre = perfil.data?.firstName ?? usuario.username
    const contenedor = useRef<HTMLDivElement>(null)

    // Cerrar al hacer clic fuera del menu
    useEffect(() => {
        if (!abierto) return
        const alClic = (e: MouseEvent) => {
            if (!contenedor.current?.contains(e.target as Node)) setAbierto(false)
        }
        document.addEventListener('mousedown', alClic)
        return () => document.removeEventListener('mousedown', alClic)
    }, [abierto])

    return (
        <div ref={contenedor} className="relative">
            <button
                type="button"
                aria-expanded={abierto}
                aria-haspopup="menu"
                onClick={() => setAbierto((a) => !a)}
                onKeyDown={(e) => e.key === 'Escape' && setAbierto(false)}
                className="focus-visible:outline-marca-600 flex min-h-11 items-center gap-2.5 rounded-full border border-slate-300 py-1 pr-3 pl-1.5 focus-visible:outline-2 dark:border-slate-700"
            >
        <span className="bg-marca-50 text-marca-700 grid size-8 place-items-center rounded-full text-xs font-semibold uppercase dark:bg-indigo-950 dark:text-indigo-300">
          {nombre.charAt(0)}
        </span>
                <span className="text-[15px] font-medium">{nombre}</span>
                <IconoChevron className="size-4 text-slate-500" />
            </button>
            {abierto && (
                <div
                    role="menu"
                    className="absolute right-0 z-40 mt-2 w-48 overflow-hidden rounded-lg bg-white py-1 shadow-lg ring-1 ring-slate-200 dark:bg-slate-900 dark:ring-slate-700"
                >
                    <Link
                        role="menuitem"
                        to={RUTAS.cuenta}
                        className={estiloItem}
                        onClick={() => setAbierto(false)}
                    >
                        Mi cuenta
                    </Link>
                    {tieneRol('USER') && (
                        <Link
                            role="menuitem"
                            to={RUTAS.misTickets}
                            className={estiloItem}
                            onClick={() => setAbierto(false)}
                        >
                            Mis tickets
                        </Link>
                    )}
                    <button role="menuitem" type="button" className={estiloItem} onClick={salir}>
                        Salir
                    </button>
                </div>
            )}
        </div>
    )
}
