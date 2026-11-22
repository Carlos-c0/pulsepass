import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import Aviso from '../componentes/Aviso'
import Boton from '../componentes/Boton'
import Campo from '../componentes/Campo'
import Cargando from '../componentes/Cargando'
import Contenedor, { TituloPagina } from '../componentes/Contenedor'
import EstadoError from '../componentes/EstadoError'
import { IconoBuscar } from '../componentes/Iconos'
import Tarjeta from '../componentes/Tarjeta'
import BadgeEstadoTicket from '../funcionalidades/tickets/componentes/BadgeEstadoTicket'
import { useBuscarTicket, useUsarTicket } from '../funcionalidades/tickets/hooks'
import { BuscarTicketSchema, type BuscarTicketForm } from '../funcionalidades/tickets/tipos'
import { esEstado, mensajeDeError } from '../lib/errores'

export default function ValidacionIngreso() {
    const [codigo, setCodigo] = useState('')
    const ticket = useBuscarTicket(codigo)
    const usar = useUsarTicket()
    const {
        register,
        handleSubmit,
        reset,
        formState: { errors },
    } = useForm<BuscarTicketForm>({ resolver: zodResolver(BuscarTicketSchema) })

    const buscar = ({ ticketCode }: BuscarTicketForm) => {
        usar.reset()
        setCodigo(ticketCode.toUpperCase())
    }

    const marcar = () => {
        if (!ticket.data) return
        // Tras el exito el campo se limpia para el siguiente ingreso (9.14)
        usar.mutate(ticket.data.ticketCode, {
            onSuccess: () => reset({ ticketCode: '' }),
            // Si otra puerta ya lo uso, se vuelve a pedir para que el badge muestre el estado real
            onError: (error) => {
                if (esEstado(error, 409)) void ticket.refetch()
            },
        })
    }

    return (
        <Contenedor estrecho className="space-y-6">
            <TituloPagina>Validación de ingreso</TituloPagina>
            <form
                onSubmit={handleSubmit(buscar)}
                noValidate
                className="flex flex-col gap-3 sm:flex-row sm:items-start"
            >
                <Campo
                    etiqueta="Código de ticket"
                    autoFocus
                    autoComplete="off"
                    className="flex-1"
                    error={errors.ticketCode?.message}
                    {...register('ticketCode')}
                />
                <Boton type="submit" className="sm:mt-[1.625rem]">
                    <IconoBuscar className="size-[18px]" />
                    Buscar
                </Boton>
            </form>

            {codigo && ticket.isPending && <Cargando filas={1} />}
            {esEstado(ticket.error, 404) && <Aviso tono="error">Ticket no encontrado.</Aviso>}
            {esEstado(ticket.error, 403) && (
                <Aviso tono="error">Sin permisos para consultar este ticket.</Aviso>
            )}
            {ticket.isError && !esEstado(ticket.error, 403, 404) && (
                <EstadoError error={ticket.error} onReintentar={() => ticket.refetch()} />
            )}

            {/* Resultado grande y de alto contraste para usarlo en la puerta (9.14) */}
            {ticket.data && (
                <Tarjeta className="space-y-5 px-7">
                    <div>
                        <div className="flex items-start justify-between gap-3">
                            <p className="font-titulo text-[1.4rem] leading-tight font-semibold">
                                {ticket.data.eventName}
                            </p>
                            <BadgeEstadoTicket estado={ticket.data.status} />
                        </div>
                        {/* TicketResponse solo trae el correo del titular */}
                        <p className="mt-2 text-lg text-slate-600 dark:text-slate-400">
                            {ticket.data.type} · Titular: {ticket.data.userEmail}
                        </p>
                    </div>
                    {ticket.data.status === 'PAID' && (
                        <Boton tamano="grande" cargando={usar.isPending} onClick={marcar} className="w-full">
                            Marcar como usado
                        </Boton>
                    )}
                </Tarjeta>
            )}

            {usar.isSuccess && (
                <Aviso tono="exito" grande>
                    <strong>Ingreso registrado.</strong>
                </Aviso>
            )}
            {/* 409: el ticket ya se uso o no esta pagado; el mensaje lo pone el backend */}
            {usar.isError && (
                <Aviso
                    tono={esEstado(usar.error, 409) ? 'advertencia' : 'error'}
                    className="px-5 py-5 text-lg"
                >
                    <strong>{mensajeDeError(usar.error)}</strong>
                    {esEstado(usar.error, 409) && <span className="font-normal"> (409)</span>}
                </Aviso>
            )}
        </Contenedor>
    )
}
