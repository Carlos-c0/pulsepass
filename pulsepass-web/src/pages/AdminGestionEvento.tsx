import { useState } from 'react'
import { useParams } from 'react-router'
import Aviso from '../componentes/Aviso'
import { useAvisos } from '../componentes/Avisos'
import Boton from '../componentes/Boton'
import Cargando from '../componentes/Cargando'
import CodigoTexto from '../componentes/CodigoTexto'
import Contenedor from '../componentes/Contenedor'
import EnlaceSeccion from '../componentes/EnlaceSeccion'
import EnlaceVolver from '../componentes/EnlaceVolver'
import EstadoError from '../componentes/EstadoError'
import { IconoMas } from '../componentes/Iconos'
import Modal from '../componentes/Modal'
import Selector from '../componentes/Selector'
import Tarjeta from '../componentes/Tarjeta'
import AvatarArtista from '../funcionalidades/artistas/componentes/AvatarArtista'
import { useArtistasActivos } from '../funcionalidades/artistas/hooks'
import BadgeEstadoEvento from '../funcionalidades/eventos/componentes/BadgeEstadoEvento'
import { useAsociarArtista, useEvento, usePublicarEvento } from '../funcionalidades/eventos/hooks'
import { esEstado, mensajeDeError } from '../lib/errores'
import { formatearFechaHora } from '../lib/formato'
import { RUTAS } from '../lib/rutas'
import NoEncontrada from './NoEncontrada'

export default function AdminGestionEvento() {
    const { eventCode = '' } = useParams()
    const evento = useEvento(eventCode)
    const artistas = useArtistasActivos()
    const publicar = usePublicarEvento()
    const asociar = useAsociarArtista()
    const { notificar } = useAvisos()
    const [confirmando, setConfirmando] = useState(false)
    const [artistaId, setArtistaId] = useState('')

    if (esEstado(evento.error, 404))
        return <NoEncontrada titulo="No existe un evento con ese código" />
    if (evento.isPending)
        return (
            <Contenedor estrecho>
                <Cargando />
            </Contenedor>
        )
    if (evento.isError)
        return (
            <Contenedor estrecho>
                <EstadoError error={evento.error} onReintentar={() => evento.refetch()} />
            </Contenedor>
        )

    const e = evento.data
    const asociados = new Set(e.artists.map((a) => a.id))
    // Solo ayuda a no elegir dos veces el mismo: el backend igual responde 409 si se repite
    const disponibles = (artistas.data ?? []).filter((a) => !asociados.has(a.id))
    const elegido = artistaId || String(disponibles[0]?.id ?? '')

    const cerrarModal = () => {
        publicar.reset()
        setConfirmando(false)
    }

    const confirmarPublicacion = () =>
        publicar.mutate(e.eventCode, {
            onSuccess: () => {
                setConfirmando(false)
                notificar('exito', 'Evento publicado.')
            },
        })

    const asociarArtista = () => {
        const artista = disponibles.find((a) => a.id === Number(elegido))
        if (!artista) return
        asociar.mutate(
            { eventCode: e.eventCode, artistId: artista.id, stageName: artista.stageName },
            {
                onSuccess: () => {
                    setArtistaId('')
                    notificar('exito', `${artista.stageName} quedó asociado al evento.`)
                },
            },
        )
    }

    return (
        <Contenedor estrecho className="space-y-5">
            <EnlaceVolver a={RUTAS.adminEventos}>Eventos</EnlaceVolver>
            <header className="space-y-1.5">
                <div className="flex items-start justify-between gap-4">
                    <h1 className="text-[1.75rem] leading-tight font-bold sm:text-[2rem]">
                        {e.name}
                        <span className="sr-only"> · {e.status}</span>
                    </h1>
                    <span className="mt-1.5" aria-hidden="true">
            <BadgeEstadoEvento estado={e.status} />
          </span>
                </div>
                <p className="text-[15px] text-slate-600 dark:text-slate-400">
                    <CodigoTexto>{e.eventCode}</CodigoTexto> · {e.category} ·{' '}
                    {formatearFechaHora(e.eventDate, ' ')} · Mín. {e.minimumAge} · {e.venueName}
                </p>
            </header>

            <Tarjeta className="space-y-3">
                <h2 className="text-lg font-semibold">Publicación</h2>
                <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                    <p className="text-[15px] text-slate-600 dark:text-slate-400">
                        El evento es visible al público una vez publicado.
                    </p>
                    <Boton disabled={e.status !== 'DRAFT'} onClick={() => setConfirmando(true)}>
                        Publicar evento
                    </Boton>
                </div>
                {publicar.isError && !confirmando && (
                    <Aviso tono="error">{mensajeDeError(publicar.error)}</Aviso>
                )}
            </Tarjeta>

            <Tarjeta className="space-y-4">
                <h2 className="text-lg font-semibold">Artistas asociados</h2>
                {e.artists.length === 0 ? (
                    <p className="text-[15px] text-slate-600 dark:text-slate-400">
                        Todavía no hay artistas asociados.
                    </p>
                ) : (
                    <ul aria-label="Artistas asociados" className="flex flex-wrap gap-5">
                        {e.artists.map((a) => (
                            <li key={a.id}>
                                <AvatarArtista artista={a} />
                            </li>
                        ))}
                    </ul>
                )}
                <div className="flex items-end gap-3">
                    <Selector
                        etiqueta="Agregar artista"
                        value={elegido}
                        onChange={(ev) => setArtistaId(ev.target.value)}
                        disabled={disponibles.length === 0}
                        className="flex-1"
                    >
                        {disponibles.length === 0 && <option value="">No quedan artistas por asociar</option>}
                        {disponibles.map((a) => (
                            <option key={a.id} value={a.id}>
                                {a.stageName}
                            </option>
                        ))}
                    </Selector>
                    <Boton
                        variante="secundario"
                        onClick={asociarArtista}
                        disabled={!elegido}
                        cargando={asociar.isPending}
                    >
                        <IconoMas className="size-[18px]" />
                        Asociar
                    </Boton>
                </div>
                {asociar.isError && <Aviso tono="error">{mensajeDeError(asociar.error)}</Aviso>}
            </Tarjeta>

            <EnlaceSeccion a={RUTAS.ticketsPagados(e.eventCode)}>Tickets pagados</EnlaceSeccion>

            <Modal
                abierto={confirmando}
                titulo="Publicar evento"
                onCerrar={cerrarModal}
                acciones={
                    <>
                        <Boton variante="secundario" onClick={cerrarModal}>
                            Cancelar
                        </Boton>
                        <Boton cargando={publicar.isPending} onClick={confirmarPublicacion}>
                            Publicar
                        </Boton>
                    </>
                }
            >
                <p>¿Publicar {e.name}? Quedará visible en la cartelera.</p>
                {publicar.isError && <Aviso tono="error">{mensajeDeError(publicar.error)}</Aviso>}
            </Modal>
        </Contenedor>
    )
}