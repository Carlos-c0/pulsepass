import { useState } from 'react'
import { useNavigate } from 'react-router'
import Aviso from '../componentes/Aviso'
import Boton from '../componentes/Boton'
import Campo from '../componentes/Campo'
import CodigoTexto from '../componentes/CodigoTexto'
import Contenedor, { TituloPagina } from '../componentes/Contenedor'
import EnlaceBoton from '../componentes/EnlaceBoton'
import { IconoMas } from '../componentes/Iconos'
import { useEventosCreados, type EventoCreado } from '../funcionalidades/admin/eventosCreados'
import BadgeEstadoEvento from '../funcionalidades/eventos/componentes/BadgeEstadoEvento'
import { useEvento } from '../funcionalidades/eventos/hooks'
import { RUTAS } from '../lib/rutas'

// El estado se pide a la API: pudo cambiar (publicado) desde que se creo
function ItemCreado({ evento }: { evento: EventoCreado }) {
    const actual = useEvento(evento.eventCode)
    return (
        <div className="flex items-center justify-between gap-3 rounded-2xl border border-slate-200 bg-white py-3 pr-4 pl-4 shadow-sm dark:border-slate-800 dark:bg-slate-900">
      <span className="min-w-0">
        <CodigoTexto className="font-bold">{evento.eventCode}</CodigoTexto> · {evento.name}
      </span>
            <span className="flex shrink-0 items-center gap-3">
        {actual.data && <BadgeEstadoEvento estado={actual.data.status} />}
                <EnlaceBoton to={RUTAS.adminEvento(evento.eventCode)} variante="secundario">
          Gestionar
        </EnlaceBoton>
      </span>
        </div>
    )
}

export default function AdminEventos() {
    const navigate = useNavigate()
    const creados = useEventosCreados()
    const [codigo, setCodigo] = useState('')

    return (
        <Contenedor estrecho className="space-y-6">
            <div className="flex flex-wrap items-center justify-between gap-3">
                <TituloPagina>Administración de eventos</TituloPagina>
                <EnlaceBoton to={RUTAS.adminNuevoEvento}>
                    <IconoMas className="size-[18px]" />
                    Nuevo evento
                </EnlaceBoton>
            </div>

            <form
                className="flex items-end gap-3"
                onSubmit={(e) => {
                    e.preventDefault()
                    if (codigo.trim()) navigate(RUTAS.adminEvento(codigo.trim().toUpperCase()))
                }}
            >
                <Campo
                    etiqueta="Buscar evento por código"
                    value={codigo}
                    placeholder="CMF-2026"
                    onChange={(e) => setCodigo(e.target.value)}
                    className="flex-1"
                />
                <Boton type="submit" variante="secundario">
                    Abrir
                </Boton>
            </form>

            <section className="space-y-4">
                <h2 className="text-lg font-semibold">Creados en esta sesión</h2>
                {creados.length === 0 ? (
                    <p className="text-[15px] text-slate-600 dark:text-slate-400">
                        Todavía no has creado eventos en esta pestaña.
                    </p>
                ) : (
                    <ul className="space-y-3">
                        {creados.map((e) => (
                            <li key={e.eventCode}>
                                <ItemCreado evento={e} />
                            </li>
                        ))}
                    </ul>
                )}
                <Aviso tono="info">Nota: la API no ofrece un listado completo de eventos (GAP-01).</Aviso>
            </section>
        </Contenedor>
    )
}
