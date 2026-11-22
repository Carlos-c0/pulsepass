import { useParams } from 'react-router'
import Badge from '../componentes/Badge'
import Cargando from '../componentes/Cargando'
import Contenedor from '../componentes/Contenedor'
import EnlaceVolver from '../componentes/EnlaceVolver'
import EstadoError from '../componentes/EstadoError'
import EstadoVacio from '../componentes/EstadoVacio'
import Tarjeta from '../componentes/Tarjeta'
import { Iniciales } from '../funcionalidades/artistas/componentes/AvatarArtista'
import { useArtista } from '../funcionalidades/artistas/hooks'
import GrillaEventos from '../funcionalidades/eventos/componentes/GrillaEventos'
import { useEventosPorArtista } from '../funcionalidades/eventos/hooks'
import { esEstado } from '../lib/errores'
import { RUTAS } from '../lib/rutas'
import NoEncontrada from './NoEncontrada'

export default function DetalleArtista() {
    const { id: idTexto } = useParams()
    const id = Number(idTexto)
    const idValido = Number.isInteger(id) && id > 0
    // Con un id que no es numero no se llama a la API (9.6); el hook queda deshabilitado
    const artista = useArtista(id)
    const eventos = useEventosPorArtista(artista.data?.stageName)

    if (!idValido) return <NoEncontrada />
    if (esEstado(artista.error, 404)) return <NoEncontrada />

    return (
        <Contenedor className="space-y-6">
            <EnlaceVolver a={RUTAS.artistas}>Artistas</EnlaceVolver>
            {artista.isPending && <Cargando filas={1} />}
            {artista.isError && (
                <EstadoError error={artista.error} onReintentar={() => artista.refetch()} />
            )}
            {artista.data && (
                <>
                    <Tarjeta className="flex flex-col items-center gap-6 text-center sm:flex-row sm:text-left">
                        <Iniciales nombre={artista.data.stageName} id={artista.data.id} tamano="xl" />
                        <div className="space-y-2">
                            <h1 className="text-[2rem] leading-tight font-bold">{artista.data.stageName}</h1>
                            <p className="text-slate-600 dark:text-slate-400">
                                Nombre artístico{artista.data.genre ? ` · ${artista.data.genre}` : ''}
                            </p>
                            <Badge tono={artista.data.active ? 'verde' : 'gris'}>
                                {artista.data.active ? 'Activo' : 'Inactivo'}
                            </Badge>
                        </div>
                    </Tarjeta>

                    <section className="space-y-4">
                        <h2 className="text-xl font-semibold">Próximos eventos de {artista.data.stageName}</h2>
                        {eventos.isPending && <Cargando forma="tarjetas" filas={1} />}
                        {eventos.isError && (
                            <EstadoError error={eventos.error} onReintentar={() => eventos.refetch()} />
                        )}
                        {eventos.data?.length === 0 && <EstadoVacio mensaje="Sin eventos próximos" />}
                        {eventos.data && eventos.data.length > 0 && <GrillaEventos eventos={eventos.data} />}
                    </section>
                </>
            )}
        </Contenedor>
    )
}
