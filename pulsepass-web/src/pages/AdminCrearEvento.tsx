import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { useNavigate } from 'react-router'
import Aviso from '../componentes/Aviso'
import { useAvisos } from '../componentes/Avisos'
import AreaTexto from '../componentes/AreaTexto'
import Boton from '../componentes/Boton'
import Campo from '../componentes/Campo'
import Contenedor, { TituloPagina } from '../componentes/Contenedor'
import EnlaceVolver from '../componentes/EnlaceVolver'
import Selector from '../componentes/Selector'
import Tarjeta from '../componentes/Tarjeta'
import { recordarEventoCreado } from '../funcionalidades/admin/eventosCreados'
import { useCrearEvento } from '../funcionalidades/eventos/hooks'
import {
    CrearEventoSchema,
    EventCategorySchema,
    type CrearEventoForm,
} from '../funcionalidades/eventos/tipos'
import { useSedesActivas } from '../funcionalidades/sedes/hooks'
import { esEstado, mensajeDeError } from '../lib/errores'
import { aplicarErroresDeCampos } from '../lib/erroresFormulario'
import { RUTAS } from '../lib/rutas'

const CAMPOS = [
    'eventCode',
    'name',
    'description',
    'category',
    'eventDate',
    'minimumAge',
    'venueCode',
] as const

export default function AdminCrearEvento() {
    const navigate = useNavigate()
    const { notificar } = useAvisos()
    const crear = useCrearEvento()
    const sedes = useSedesActivas()
    const [avisoGeneral, setAvisoGeneral] = useState<string | null>(null)
    const {
        register,
        handleSubmit,
        setError,
        control,
        formState: { errors },
    } = useForm<CrearEventoForm>({
        resolver: zodResolver(CrearEventoSchema),
        defaultValues: { description: '', category: 'MUSIC', minimumAge: 0, venueCode: '' },
    })
    const descripcion = useWatch({ control, name: 'description' })

    const enviar = (datos: CrearEventoForm) => {
        setAvisoGeneral(null)
        crear.mutate(
            { ...datos, eventCode: datos.eventCode.toUpperCase() },
            {
                onSuccess: (evento) => {
                    recordarEventoCreado({ eventCode: evento.eventCode, name: evento.name })
                    notificar('exito', 'Evento creado como borrador.')
                    navigate(RUTAS.adminEvento(evento.eventCode))
                },
                onError: (error) => {
                    if (esEstado(error, 403)) {
                        navigate(RUTAS.sinPermisos)
                        return
                    }
                    const sueltos = aplicarErroresDeCampos(error, CAMPOS, setError)
                    if (!esEstado(error, 400) || sueltos.length > 0) {
                        setAvisoGeneral(sueltos.join(' ') || mensajeDeError(error))
                    }
                },
            },
        )
    }

    return (
        <Contenedor className="max-w-[47.5rem] space-y-6">
            <EnlaceVolver a={RUTAS.adminEventos}>Eventos</EnlaceVolver>
            <TituloPagina>Nuevo evento</TituloPagina>
            <Tarjeta className="px-7 py-7">
                <form
                    onSubmit={handleSubmit(enviar)}
                    noValidate
                    className="grid gap-4 sm:grid-cols-[1fr_0.85fr]"
                >
                    {avisoGeneral && (
                        <Aviso tono="error" className="sm:col-span-2">
                            {avisoGeneral}
                        </Aviso>
                    )}
                    <Campo
                        etiqueta="Código del evento"
                        placeholder="CMF-2026"
                        className="sm:col-span-2"
                        error={errors.eventCode?.message}
                        {...register('eventCode')}
                    />
                    <Campo
                        etiqueta="Nombre"
                        className="sm:col-span-2"
                        error={errors.name?.message}
                        {...register('name')}
                    />
                    <div className="sm:col-span-2">
                        <AreaTexto
                            etiqueta="Descripción"
                            rows={4}
                            maxLength={2000}
                            error={errors.description?.message}
                            {...register('description')}
                        />
                        <p className="mt-1 text-right text-[13px] text-slate-600 dark:text-slate-400">
                            {(descripcion ?? '').length}/2000
                        </p>
                    </div>
                    <Selector etiqueta="Categoría" error={errors.category?.message} {...register('category')}>
                        {EventCategorySchema.options.map((c) => (
                            <option key={c} value={c}>
                                {c}
                            </option>
                        ))}
                    </Selector>
                    <Campo
                        etiqueta="Edad mínima"
                        type="number"
                        min={0}
                        error={errors.minimumAge?.message}
                        {...register('minimumAge', { valueAsNumber: true })}
                    />
                    <Campo
                        etiqueta="Fecha y hora"
                        type="datetime-local"
                        className="sm:col-span-2"
                        error={errors.eventDate?.message}
                        {...register('eventDate')}
                    />
                    <Selector
                        etiqueta="Sede"
                        className="sm:col-span-2"
                        error={errors.venueCode?.message}
                        disabled={sedes.isPending}
                        {...register('venueCode')}
                    >
                        <option value="">{sedes.isPending ? 'Cargando sedes…' : 'Elige una sede'}</option>
                        {sedes.data?.map((s) => (
                            <option key={s.code} value={s.code}>
                                {s.name} ({s.code})
                            </option>
                        ))}
                    </Selector>
                    <div className="mt-2 flex justify-end gap-3 sm:col-span-2">
                        <Boton variante="secundario" onClick={() => navigate(RUTAS.adminEventos)}>
                            Cancelar
                        </Boton>
                        <Boton type="submit" cargando={crear.isPending}>
                            Crear evento
                        </Boton>
                    </div>
                </form>
            </Tarjeta>
        </Contenedor>
    )
}
