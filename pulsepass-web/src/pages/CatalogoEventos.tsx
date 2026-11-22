import { Link, useSearchParams } from 'react-router'
import Cargando from '../componentes/Cargando'
import Contenedor, { TituloPagina } from '../componentes/Contenedor'
import EnlaceBoton from '../componentes/EnlaceBoton'
import EstadoError from '../componentes/EstadoError'
import EstadoVacio from '../componentes/EstadoVacio'
import { useArtistaPorNombre, useArtistasActivos } from '../funcionalidades/artistas/hooks'
import BarraFiltros, { type Filtros } from '../funcionalidades/eventos/componentes/BarraFiltros'
import GrillaEventos from '../funcionalidades/eventos/componentes/GrillaEventos'
import { useEventosPorArtista, useEventosPublicados } from '../funcionalidades/eventos/hooks'
import { RUTAS } from '../lib/rutas'

export default function CatalogoEventos() {
    // Los filtros viven en la URL: se pueden compartir y sobreviven a recargar (FR-FE-EVT-002)
    const [params, setParams] = useSearchParams()
    const filtros: Filtros = {
        q: params.get('q') ?? '',
        categoria: params.get('categoria') ?? '',
        artista: params.get('artista') ?? '',
    }

    const publicados = useEventosPublicados()
    const porArtista = useEventosPorArtista(filtros.artista || undefined)
    const artistas = useArtistasActivos()
    const artistaElegido = useArtistaPorNombre(filtros.artista)
    const consulta = filtros.artista ? porArtista : publicados

    const cambiar = (cambio: Partial<Filtros>) => {
        const siguiente = { ...filtros, ...cambio }
        const nuevos = new URLSearchParams()
        if (siguiente.q) nuevos.set('q', siguiente.q)
        if (siguiente.categoria) nuevos.set('categoria', siguiente.categoria)
        if (siguiente.artista) nuevos.set('artista', siguiente.artista)
        setParams(nuevos, { replace: true })
    }

    const texto = filtros.q.trim().toLowerCase()
    const visibles = (consulta.data ?? []).filter(
        (e) =>
            (!texto || e.name.toLowerCase().includes(texto)) &&
            (!filtros.categoria || e.category === filtros.categoria),
    )

    return (
        <Contenedor className="space-y-6">
            <TituloPagina>Cartelera</TituloPagina>
            <BarraFiltros
                filtros={filtros}
                artistas={artistas.data ?? []}
                onCambiar={cambiar}
                onLimpiar={() => setParams(new URLSearchParams(), { replace: true })}
            />
            {artistaElegido.data && (
                <p className="text-[15px]">
                    Eventos de{' '}
                    <Link
                        to={RUTAS.artista(artistaElegido.data.id)}
                        className="text-marca-700 font-semibold dark:text-indigo-300"
                    >
                        {artistaElegido.data.stageName}
                    </Link>
                </p>
            )}

            {consulta.isPending && <Cargando forma="tarjetas" filas={6} />}
            {consulta.isError && (
                <EstadoError error={consulta.error} onReintentar={() => consulta.refetch()} />
            )}
            {consulta.data && (
                <>
                    <p className="-mt-1 text-[15px] text-slate-600 dark:text-slate-400" aria-live="polite">
                        {visibles.length} {visibles.length === 1 ? 'evento' : 'eventos'}
                    </p>
                    {visibles.length === 0 ? (
                        <EstadoVacio
                            mensaje={
                                filtros.q || filtros.categoria || filtros.artista
                                    ? 'Sin resultados para tu búsqueda'
                                    : 'Aún no hay eventos publicados'
                            }
                            accion={
                                <EnlaceBoton to={RUTAS.artistas} variante="secundario">
                                    Ver artistas
                                </EnlaceBoton>
                            }
                        />
                    ) : (
                        <GrillaEventos eventos={visibles} />
                    )}
                </>
            )}
        </Contenedor>
    )
}
