import Cargando from '../componentes/Cargando'
import Contenedor from '../componentes/Contenedor'
import EnlaceBoton from '../componentes/EnlaceBoton'
import EstadoError from '../componentes/EstadoError'
import EstadoVacio from '../componentes/EstadoVacio'
import EnlaceSeccion from '../componentes/EnlaceSeccion'
import AvatarArtista from '../funcionalidades/artistas/componentes/AvatarArtista'
import { useArtistasActivos } from '../funcionalidades/artistas/hooks'
import { useAuth } from '../funcionalidades/auth/AuthContext'
import GrillaEventos from '../funcionalidades/eventos/componentes/GrillaEventos'
import { useEventosPublicados } from '../funcionalidades/eventos/hooks'
import { RUTAS } from '../lib/rutas'

export default function Inicio() {
    const { sesion } = useAuth()
    const eventos = useEventosPublicados()
    const artistas = useArtistasActivos()

    // Los tres mas cercanos en el tiempo
    const proximos = [...(eventos.data ?? [])]
        .sort((a, b) => a.eventDate.localeCompare(b.eventDate))
        .slice(0, 3)

    return (
        <>
            {/* DES-11: degradado de marca-600 a acento-500 de borde a borde */}
            <section className="from-marca-600 via-marca-600 to-acento-500 bg-linear-to-r via-40% text-white">
                <div className="mx-auto max-w-6xl px-4 py-8 sm:py-18">
                    <h1 className="max-w-xl text-[2rem] leading-tight font-bold sm:text-5xl sm:leading-[1.1]">
                        Descubre los mejores eventos
                    </h1>
                    <p className="mt-3 max-w-xl text-lg text-white/95 sm:mt-6">
                        Música, cultura, deporte y tecnología en un solo lugar.
                    </p>
                    <div className="mt-5 flex flex-col gap-3 sm:mt-8 sm:flex-row">
                        <EnlaceBoton
                            to={RUTAS.eventos}
                            className="text-marca-700 hover:bg-marca-50 bg-white sm:min-h-13 sm:px-5 sm:text-base"
                            variante="fantasma"
                        >
                            Ver cartelera
                        </EnlaceBoton>
                        {sesion.estado !== 'autenticado' && (
                            <EnlaceBoton
                                to={RUTAS.registro}
                                variante="fantasma"
                                className="border border-white/90 text-white hover:bg-white/10 sm:min-h-13 sm:px-5 sm:text-base dark:text-white"
                            >
                                Crear cuenta
                            </EnlaceBoton>
                        )}
                    </div>
                </div>
            </section>

            <Contenedor className="space-y-10">
                <section className="space-y-5">
                    <div className="flex items-center justify-between">
                        <h2 className="text-xl font-semibold">Próximos eventos</h2>
                        <span className="hidden sm:block">
              <EnlaceSeccion a={RUTAS.eventos}>Ver todos</EnlaceSeccion>
            </span>
                    </div>
                    {eventos.isPending && <Cargando forma="tarjetas" />}
                    {eventos.isError && (
                        <EstadoError error={eventos.error} onReintentar={() => eventos.refetch()} />
                    )}
                    {eventos.data?.length === 0 && (
                        <EstadoVacio
                            mensaje="Aún no hay eventos publicados"
                            accion={
                                <EnlaceBoton to={RUTAS.artistas} variante="secundario">
                                    Ver artistas
                                </EnlaceBoton>
                            }
                        />
                    )}
                    {proximos.length > 0 && <GrillaEventos eventos={proximos} />}
                </section>

                <section className="space-y-5">
                    <div className="flex items-center justify-between">
                        <h2 className="text-xl font-semibold">Artistas destacados</h2>
                        <span className="hidden sm:block">
              <EnlaceSeccion a={RUTAS.artistas}>Ver todos</EnlaceSeccion>
            </span>
                    </div>
                    {artistas.isPending && <Cargando filas={1} />}
                    {artistas.isError && (
                        <EstadoError error={artistas.error} onReintentar={() => artistas.refetch()} />
                    )}
                    {artistas.data && (
                        <ul className="flex flex-col gap-4 sm:flex-row sm:flex-wrap sm:gap-8">
                            {artistas.data.slice(0, 6).map((a) => (
                                <li key={a.id} className="font-semibold">
                                    <AvatarArtista artista={a} tamano="md" />
                                </li>
                            ))}
                        </ul>
                    )}
                </section>
            </Contenedor>
        </>
    )
}
