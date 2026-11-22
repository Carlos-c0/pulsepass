import Cargando from '../componentes/Cargando'
import Contenedor, { TituloPagina } from '../componentes/Contenedor'
import EstadoError from '../componentes/EstadoError'
import EstadoVacio from '../componentes/EstadoVacio'
import TarjetaArtista from '../funcionalidades/artistas/componentes/TarjetaArtista'
import { useArtistasActivos } from '../funcionalidades/artistas/hooks'

export default function ListaArtistas() {
    const artistas = useArtistasActivos()

    return (
        <Contenedor className="space-y-6">
            <TituloPagina>Artistas</TituloPagina>
            {artistas.isPending && <Cargando forma="tarjetas" />}
            {artistas.isError && (
                <EstadoError error={artistas.error} onReintentar={() => artistas.refetch()} />
            )}
            {artistas.data?.length === 0 && <EstadoVacio mensaje="Aún no hay artistas activos" />}
            {artistas.data && artistas.data.length > 0 && (
                <ul className="grid grid-cols-2 gap-4 sm:grid-cols-3 sm:gap-6 lg:grid-cols-4">
                    {artistas.data.map((a) => (
                        <li key={a.id}>
                            <TarjetaArtista artista={a} />
                        </li>
                    ))}
                </ul>
            )}
        </Contenedor>
    )
}
