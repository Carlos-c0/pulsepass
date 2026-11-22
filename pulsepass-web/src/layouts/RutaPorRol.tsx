import { Navigate, Outlet, useLocation } from 'react-router'
import Cargando from '../componentes/Cargando'
import Contenedor from '../componentes/Contenedor'
import { useAuth } from '../funcionalidades/auth/AuthContext'
import type { Rol } from '../funcionalidades/auth/tipos'
import { RUTAS } from '../lib/rutas'

interface RutaPorRolProps {
    roles: Rol[]
}

export default function RutaPorRol({ roles }: RutaPorRolProps) {
    const { sesion } = useAuth()
    const location = useLocation()

    if (sesion.estado === 'cargando')
        return (
            <Contenedor>
                <Cargando />
            </Contenedor>
        )
    if (sesion.estado === 'anonimo') {
        return <Navigate to={RUTAS.login} replace state={{ desde: location.pathname }} />
    }
    if (!roles.some((r) => sesion.usuario.roles.includes(r))) {
        return <Navigate to={RUTAS.sinPermisos} replace />
    }
    return <Outlet />
}
