import { Navigate, Outlet, useLocation } from 'react-router'
import Cargando from '../componentes/Cargando'
import Contenedor from '../componentes/Contenedor'
import { useAuth } from '../funcionalidades/auth/AuthContext'
import { RUTAS } from '../lib/rutas'

// Solo es experiencia de usuario: quien protege de verdad es el backend (FE-004)
export default function RutaProtegida() {
    const { sesion } = useAuth()
    const location = useLocation()

    if (sesion.estado === 'cargando')
        return (
            <Contenedor>
                <Cargando />
            </Contenedor>
        )
    if (sesion.estado === 'anonimo') {
        return (
            <Navigate
                to={RUTAS.login}
                replace
                state={{ desde: location.pathname, motivo: sesion.motivo }}
            />
        )
    }
    return <Outlet />
}
