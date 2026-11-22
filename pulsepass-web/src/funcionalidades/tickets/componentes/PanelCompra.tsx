import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { useLocation, useNavigate } from 'react-router'
import Boton from '../../../componentes/Boton'
import Tarjeta from '../../../componentes/Tarjeta'
import { RUTAS } from '../../../lib/rutas'
import { useAuth } from '../../auth/AuthContext'
import type { Evento } from '../../eventos/tipos'
import { CompraSchema, TicketTypeSchema, type CompraForm } from '../tipos'
import ModalConfirmarCompra from './ModalConfirmarCompra'

const MOTIVO_NO_DISPONIBLE: Record<Exclude<Evento['status'], 'PUBLISHED'>, string> = {
    DRAFT: 'El evento todavía no está publicado.',
    SOLD_OUT: 'Entradas agotadas.',
    CANCELLED: 'El evento fue cancelado.',
    FINISHED: 'El evento ya terminó.',
}

export default function PanelCompra({ evento }: { evento: Evento }) {
    const { sesion, tieneRol } = useAuth()
    const navigate = useNavigate()
    const location = useLocation()
    const [confirmando, setConfirmando] = useState(false)
    const { register, handleSubmit, control } = useForm<CompraForm>({
        resolver: zodResolver(CompraSchema),
        defaultValues: { type: 'GENERAL' },
    })
    const tipo = useWatch({ control, name: 'type' })

    // STAFF y ADMIN no compran desde la UI: la API solo autoriza la compra a USER
    if (sesion.estado === 'autenticado' && !tieneRol('USER')) return null

    const disponible = evento.status === 'PUBLISHED'
    const anonimo = sesion.estado !== 'autenticado'

    const irALogin = () => navigate(RUTAS.login, { state: { desde: location.pathname } })

    const formulario = (
        <form id="form-compra" onSubmit={handleSubmit(() => setConfirmando(true))}>
            <fieldset className="space-y-2">
                <legend className="mb-3 text-sm text-slate-600 dark:text-slate-400">Tipo de entrada</legend>
                {TicketTypeSchema.options.map((tipo) => (
                    <label
                        key={tipo}
                        className="has-checked:border-marca-600 has-checked:bg-marca-50 flex min-h-12 cursor-pointer items-center gap-3 rounded-lg border border-slate-300 bg-white px-4 has-disabled:cursor-not-allowed has-disabled:opacity-60 dark:border-slate-700 dark:bg-slate-900 dark:has-checked:bg-indigo-950"
                    >
                        <input
                            type="radio"
                            value={tipo}
                            disabled={!disponible}
                            className="accent-marca-600 size-[18px]"
                            {...register('type')}
                        />
                        <span className="text-[17px]">{tipo}</span>
                    </label>
                ))}
            </fieldset>
            {evento.status !== 'PUBLISHED' && (
                <p className="mt-3 text-sm text-slate-600 dark:text-slate-400">
                    {MOTIVO_NO_DISPONIBLE[evento.status]}
                </p>
            )}
        </form>
    )

    const boton = anonimo ? (
        <Boton tamano="grande" onClick={irALogin} className="w-full">
            Inicia sesión para comprar
        </Boton>
    ) : (
        <Boton
            tamano="grande"
            type="submit"
            form="form-compra"
            disabled={!disponible}
            className="w-full"
        >
            Comprar entrada
        </Boton>
    )

    return (
        <>
            <Tarjeta className="space-y-4">
                <h2 className="text-lg font-semibold">Entradas</h2>
                {formulario}
                {boton}
                <p className="text-[13px] text-slate-600 dark:text-slate-400">
                    El precio lo define el sistema al confirmar.
                </p>
            </Tarjeta>

            {/* En movil la accion tambien queda fija abajo (DES-07) */}
            <div
                id="barra-compra"
                className="fixed inset-x-0 bottom-0 z-20 border-t border-slate-200 bg-white p-4 md:hidden dark:border-slate-800 dark:bg-slate-900"
            >
                {boton}
            </div>

            {sesion.estado === 'autenticado' && (
                <ModalConfirmarCompra
                    abierto={confirmando}
                    onCerrar={() => setConfirmando(false)}
                    eventCode={evento.eventCode}
                    nombreEvento={evento.name}
                    tipo={tipo}
                    email={sesion.usuario.email}
                />
            )}
        </>
    )
}
