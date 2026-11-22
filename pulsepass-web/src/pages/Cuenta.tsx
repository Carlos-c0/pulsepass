import Badge from '../componentes/Badge'
import Boton from '../componentes/Boton'
import Cargando from '../componentes/Cargando'
import Contenedor, { TituloPagina } from '../componentes/Contenedor'
import EnlaceBoton from '../componentes/EnlaceBoton'
import EstadoError from '../componentes/EstadoError'
import { IconoTicket } from '../componentes/Iconos'
import Tarjeta from '../componentes/Tarjeta'
import { useAuth } from '../funcionalidades/auth/AuthContext'
import { useSalir } from '../funcionalidades/auth/hooks'
import BuscarUsuario from '../funcionalidades/usuarios/componentes/BuscarUsuario'
import { useUsuarioPorEmail } from '../funcionalidades/usuarios/hooks'
import { formatearFechaNumerica } from '../lib/formato'
import { RUTAS } from '../lib/rutas'

export default function Cuenta() {
    const { sesion, tieneRol } = useAuth()
    const salir = useSalir()
    const email = sesion.estado === 'autenticado' ? sesion.usuario.email : ''
    const perfil = useUsuarioPorEmail(email)

    if (sesion.estado !== 'autenticado') return null
    const { usuario } = sesion
    const nombreCompleto =
        [perfil.data?.firstName, perfil.data?.lastName].filter(Boolean).join(' ') || usuario.username

    return (
        <Contenedor className="space-y-6">
            <TituloPagina>Mi cuenta</TituloPagina>
            {perfil.isPending && <Cargando filas={1} />}
            {perfil.isError && <EstadoError error={perfil.error} onReintentar={() => perfil.refetch()} />}
            {perfil.data && (
                <Tarjeta className="flex flex-col gap-5 px-7 sm:flex-row sm:items-center">
          <span className="bg-marca-50 text-marca-700 font-titulo grid size-18 shrink-0 place-items-center rounded-full text-2xl font-semibold uppercase dark:bg-indigo-950 dark:text-indigo-300">
            {nombreCompleto.charAt(0)}
          </span>
                    <div className="flex-1 space-y-1">
                        <p className="font-titulo text-[1.375rem] font-semibold">{nombreCompleto}</p>
                        <p className="text-slate-600 dark:text-slate-400">
                            @{usuario.username} · {usuario.email}
                        </p>
                        {perfil.data.birthDate && (
                            <p className="text-sm text-slate-600 dark:text-slate-400">
                                Nacimiento: {formatearFechaNumerica(perfil.data.birthDate)}
                            </p>
                        )}
                    </div>
                    <dl className="space-y-2 text-[15px]">
                        <div className="flex items-center gap-1.5">
                            <dt>Rol:</dt>
                            <dd className="flex gap-1.5">
                                {usuario.roles.map((r) => (
                                    <Badge key={r} tono="marca">
                                        {r}
                                    </Badge>
                                ))}
                            </dd>
                        </div>
                        <div className="flex items-center gap-1.5">
                            <dt>Estado:</dt>
                            <dd>
                                <Badge tono={perfil.data.active ? 'verde' : 'gris'}>
                                    {perfil.data.active ? 'Activa' : 'Inactiva'}
                                </Badge>
                            </dd>
                        </div>
                    </dl>
                </Tarjeta>
            )}
            <div className="flex flex-wrap justify-between gap-3">
                {tieneRol('USER') ? (
                    <EnlaceBoton to={RUTAS.misTickets}>
                        <IconoTicket className="size-[18px]" />
                        Ver mis tickets
                    </EnlaceBoton>
                ) : (
                    <span />
                )}
                <Boton variante="secundario" onClick={salir}>
                    Cerrar sesión
                </Boton>
            </div>
            {/* Consultar otras cuentas solo lo permite la API a ADMIN */}
            {tieneRol('ADMIN') && <BuscarUsuario />}
        </Contenedor>
    )
}
