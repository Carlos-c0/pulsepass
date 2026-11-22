import Cargando from '../componentes/Cargando'
import Contenedor, { TituloPagina } from '../componentes/Contenedor'
import EstadoError from '../componentes/EstadoError'
import EstadoVacio from '../componentes/EstadoVacio'
import TarjetaSede from '../funcionalidades/sedes/componentes/TarjetaSede'
import { useSedesActivas } from '../funcionalidades/sedes/hooks'

export default function ListaSedes() {
    const sedes = useSedesActivas()

    return (
        <Contenedor className="space-y-6">
            <TituloPagina>Sedes</TituloPagina>
            {sedes.isPending && <Cargando forma="tarjetas" />}
            {sedes.isError && <EstadoError error={sedes.error} onReintentar={() => sedes.refetch()} />}
            {sedes.data?.length === 0 && <EstadoVacio mensaje="Aún no hay sedes activas" />}
            {sedes.data && sedes.data.length > 0 && (
                <ul className="grid gap-4 sm:grid-cols-2 sm:gap-6 lg:grid-cols-3">
                    {sedes.data.map((s) => (
                        <li key={s.code}>
                            <TarjetaSede sede={s} />
                        </li>
                    ))}
                </ul>
            )}
        </Contenedor>
    )
}
