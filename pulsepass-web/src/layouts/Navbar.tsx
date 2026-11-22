import clsx from 'clsx'
import { useRef, useState } from 'react'
import { Link, NavLink, useLocation } from 'react-router'
import EnlaceBoton from '../componentes/EnlaceBoton'
import { IconoCerrar, IconoMenu, Logo } from '../componentes/Iconos'
import { useAuth } from '../funcionalidades/auth/AuthContext'
import { useSalir } from '../funcionalidades/auth/hooks'
import { RUTAS } from '../lib/rutas'
import { enlacesPara } from './enlacesNavegacion'
import MenuUsuario from './MenuUsuario'

const estiloEnlace = (isActive: boolean) =>
    clsx(
        'rounded-lg px-3 py-2.5 text-[15px] font-medium focus-visible:outline-2 focus-visible:outline-marca-600',
        isActive
            ? 'bg-marca-50 text-marca-700 dark:bg-indigo-950 dark:text-indigo-300'
            : 'text-slate-600 hover:text-slate-900 dark:text-slate-400 dark:hover:text-white',
    )

const estiloEnlaceMovil = (isActive: boolean) =>
    clsx(
        'block rounded-lg px-3 py-3 text-base focus-visible:outline-2 focus-visible:outline-marca-600',
        isActive
            ? 'bg-marca-50 text-marca-700 dark:bg-indigo-950 dark:text-indigo-300'
            : 'text-slate-900 dark:text-slate-100',
    )

export default function Navbar() {
    const { sesion } = useAuth()
    const { pathname } = useLocation()
    const salir = useSalir()
    const [menuAbierto, setMenuAbierto] = useState(false)

    const roles = sesion.estado === 'autenticado' ? sesion.usuario.roles : []
    const enlaces = enlacesPara(roles)
    const botonMenu = useRef<HTMLButtonElement>(null)
    const cerrarMenu = () => setMenuAbierto(false)
    // Con Escape el foco vuelve al boton que abrio el menu, para no perderlo en el teclado
    const cerrarConTeclado = () => {
        cerrarMenu()
        botonMenu.current?.focus()
    }

    return (
        <header className="sticky top-0 z-30 border-b border-slate-200 bg-white dark:border-slate-800 dark:bg-slate-900">
            <div className="mx-auto flex h-16 max-w-6xl items-center gap-6 px-4">
                <Link
                    to={RUTAS.inicio}
                    className="font-titulo flex items-center gap-2.5 text-xl font-bold focus-visible:outline-2"
                >
                    <Logo />
                    PulsePass
                </Link>

                <nav aria-label="Principal" className="hidden flex-1 items-center gap-1 md:flex">
                    {enlaces.map((e) => (
                        <NavLink
                            key={e.a}
                            to={e.a}
                            className={({ isActive }) =>
                                estiloEnlace(isActive || (!!e.prefijo && pathname.startsWith(e.prefijo)))
                            }
                        >
                            {e.texto}
                        </NavLink>
                    ))}
                </nav>

                <div className="ml-auto hidden items-center gap-3 md:flex">
                    {sesion.estado === 'autenticado' ? (
                        <MenuUsuario usuario={sesion.usuario} />
                    ) : (
                        <>
                            <Link
                                to={RUTAS.login}
                                className="text-marca-700 px-3 py-2 text-[15px] font-semibold dark:text-indigo-300"
                            >
                                Ingresar
                            </Link>
                            <EnlaceBoton to={RUTAS.registro}>Registrarse</EnlaceBoton>
                        </>
                    )}
                </div>

                <button
                    ref={botonMenu}
                    type="button"
                    aria-expanded={menuAbierto}
                    aria-controls="menu-movil"
                    onClick={() => setMenuAbierto((a) => !a)}
                    className="focus-visible:outline-marca-600 ml-auto rounded-lg p-2 focus-visible:outline-2 md:hidden"
                >
                    {menuAbierto ? <IconoCerrar className="size-6" /> : <IconoMenu className="size-6" />}
                    <span className="sr-only">{menuAbierto ? 'Cerrar menú' : 'Abrir menú'}</span>
                </button>
            </div>

            {menuAbierto && (
                <nav
                    id="menu-movil"
                    aria-label="Menú móvil"
                    onKeyDown={(e) => e.key === 'Escape' && cerrarConTeclado()}
                    className="px-4 pb-4 md:hidden"
                >
                    <ul className="flex flex-col">
                        {enlaces.map((e) => (
                            <li key={e.a}>
                                <NavLink
                                    to={e.a}
                                    className={({ isActive }) =>
                                        estiloEnlaceMovil(isActive || (!!e.prefijo && pathname.startsWith(e.prefijo)))
                                    }
                                    onClick={cerrarMenu}
                                >
                                    {e.texto}
                                </NavLink>
                            </li>
                        ))}
                    </ul>
                    <div className="mt-2 flex gap-3 border-t border-slate-200 px-3 pt-4 dark:border-slate-800">
                        {sesion.estado === 'autenticado' ? (
                            <>
                                <EnlaceBoton to={RUTAS.cuenta} variante="secundario" onClick={cerrarMenu}>
                                    Mi cuenta
                                </EnlaceBoton>
                                <button
                                    type="button"
                                    className="min-h-11 rounded-lg px-4 text-[15px] font-semibold text-red-600"
                                    onClick={() => {
                                        cerrarMenu()
                                        void salir()
                                    }}
                                >
                                    Salir
                                </button>
                            </>
                        ) : (
                            <>
                                <EnlaceBoton to={RUTAS.login} variante="secundario" onClick={cerrarMenu}>
                                    Ingresar
                                </EnlaceBoton>
                                <EnlaceBoton to={RUTAS.registro} onClick={cerrarMenu}>
                                    Registrarse
                                </EnlaceBoton>
                            </>
                        )}
                    </div>
                </nav>
            )}
        </header>
    )
}
