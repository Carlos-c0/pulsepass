import { Navigate, Outlet, useLocation } from 'react-router'
import Cargando from '../componentes/Cargando'
import Contenedor from '../componentes/Contenedor'
import { useAuth } from '../funcionalidades/auth/AuthContext'
import { leerEstadoNavegacion } from '../lib/estadoNavegacion'
import { RUTAS } from '../lib/rutas'

// /login y /registro no tienen sentido con la sesion iniciada.
// Si se llego aqui desde una ruta protegida, se vuelve a ella (FR-FE-AUTH-002).
export default function SoloAnonimo() {
    const { sesion } = useAuth()
    const { desde } = leerEstadoNavegacion(useLocation().state)

    if (sesion.estado === 'cargando')
        return (
            <Contenedor>
                <Cargando />
            </Contenedor>
        )
    if (sesion.estado === 'autenticado') return <Navigate to={desde ?? RUTAS.inicio} replace />
    return <Outlet />
}
