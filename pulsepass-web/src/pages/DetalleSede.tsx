import { useParams } from 'react-router'
import Badge from '../componentes/Badge'
import Cargando from '../componentes/Cargando'
import Contenedor from '../componentes/Contenedor'
import EnlaceVolver from '../componentes/EnlaceVolver'
import EstadoError from '../componentes/EstadoError'
import { Dato, ListaDatos } from '../componentes/ListaDatos'
import Tarjeta from '../componentes/Tarjeta'
import { useSede } from '../funcionalidades/sedes/hooks'
import { esEstado } from '../lib/errores'
import { RUTAS } from '../lib/rutas'
import NoEncontrada from './NoEncontrada'

const numero = new Intl.NumberFormat('es-CO')

export default function DetalleSede() {
    const { code = '' } = useParams()
    const sede = useSede(code)

    if (esEstado(sede.error, 404)) return <NoEncontrada />

    return (
        <Contenedor estrecho className="max-w-[44rem] space-y-6">
            <EnlaceVolver a={RUTAS.sedes}>Sedes</EnlaceVolver>
            {sede.isPending && <Cargando filas={1} />}
            {sede.isError && <EstadoError error={sede.error} onReintentar={() => sede.refetch()} />}
            {sede.data && (
                <Tarjeta className="px-7">
                    <div className="flex items-start justify-between gap-4">
                        <h1 className="text-[1.75rem] leading-tight font-bold sm:text-[2rem]">
                            {sede.data.name}
                        </h1>
                        <span className="mt-2">
              <Badge tono={sede.data.active ? 'verde' : 'gris'}>
                {sede.data.active ? 'Activa' : 'Inactiva'}
              </Badge>
            </span>
                    </div>
                    <div className="mt-2 border-b border-slate-200 dark:border-slate-800">
                        <ListaDatos>
                            <Dato etiqueta="Código">{sede.data.code}</Dato>
                            <Dato etiqueta="Ciudad">{sede.data.city}</Dato>
                            {sede.data.address && <Dato etiqueta="Dirección">{sede.data.address}</Dato>}
                            <Dato etiqueta="Capacidad">{numero.format(sede.data.capacity)} personas</Dato>
                            <Dato etiqueta="Estado">{sede.data.active ? 'Activa' : 'Inactiva'}</Dato>
                        </ListaDatos>
                    </div>
                </Tarjeta>
            )}
        </Contenedor>
    )
}
