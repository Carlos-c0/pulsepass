import { Link, Outlet } from 'react-router'
import BotonTema from '../componentes/BotonTema'
import { RUTAS } from '../lib/rutas'
import Navbar from './Navbar'

// Cada pagina pone su propio contenedor (normal o estrecho, DES-04);
// asi el degradado de Inicio puede ocupar todo el ancho.
export default function Layout() {
    return (
        <div className="flex min-h-dvh flex-col">
            <a
                href="#contenido"
                className="bg-marca-600 sr-only z-50 rounded px-4 py-2 text-white focus:not-sr-only focus:fixed focus:top-2 focus:left-2"
            >
                Saltar al contenido
            </a>
            <Navbar />
            <main id="contenido" className="w-full flex-1">
                <Outlet />
            </main>
            <footer className="border-t border-slate-200 bg-white dark:border-slate-800 dark:bg-slate-900">
                <div className="mx-auto flex max-w-6xl flex-col gap-3 px-4 py-6 text-[15px] text-slate-600 sm:flex-row sm:items-center sm:justify-between dark:text-slate-400">
                    <p>© 2026 PulsePass · Caso de estudio académico</p>
                    <nav aria-label="Pie de página" className="flex items-center gap-4">
                        <Link to={RUTAS.sedes} className="hover:text-slate-900 dark:hover:text-white">
                            Sedes
                        </Link>
                        <Link to={RUTAS.artistas} className="hover:text-slate-900 dark:hover:text-white">
                            Artistas
                        </Link>
                        <BotonTema />
                    </nav>
                </div>
            </footer>
        </div>
    )
}
