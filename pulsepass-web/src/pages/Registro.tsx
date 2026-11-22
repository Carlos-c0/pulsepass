import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { Link, useNavigate } from 'react-router'
import Aviso from '../componentes/Aviso'
import Boton from '../componentes/Boton'
import Campo from '../componentes/Campo'
import Contenedor from '../componentes/Contenedor'
import { Logo } from '../componentes/Iconos'
import Tarjeta from '../componentes/Tarjeta'
import MedidorFortaleza from '../funcionalidades/usuarios/componentes/MedidorFortaleza'
import { useRegistrarUsuario } from '../funcionalidades/usuarios/hooks'
import { RegistroSchema, type RegistroForm } from '../funcionalidades/usuarios/tipos'
import { esEstado, mensajeDeError } from '../lib/errores'
import { aplicarErroresDeCampos } from '../lib/erroresFormulario'
import { RUTAS } from '../lib/rutas'

const CAMPOS = ['username', 'email', 'firstName', 'lastName', 'birthDate', 'password'] as const

export default function Registro() {
    const navigate = useNavigate()
    const registrar = useRegistrarUsuario()
    const [avisoGeneral, setAvisoGeneral] = useState<string | null>(null)
    const {
        register,
        handleSubmit,
        setError,
        control,
        formState: { errors },
    } = useForm<RegistroForm>({ resolver: zodResolver(RegistroSchema) })
    const password = useWatch({ control, name: 'password' })

    const enviar = (datos: RegistroForm) => {
        setAvisoGeneral(null)
        registrar.mutate(datos, {
            // El registro no entrega token (GAP-07): se va al login con un aviso
            onSuccess: () => navigate(RUTAS.login, { state: { registrado: true } }),
            onError: (error) => {
                if (esEstado(error, 409)) {
                    setAvisoGeneral('El usuario o correo ya está registrado.')
                    return
                }
                const sueltos = aplicarErroresDeCampos(error, CAMPOS, setError)
                if (!esEstado(error, 400) || sueltos.length > 0) {
                    setAvisoGeneral(sueltos.join(' ') || mensajeDeError(error))
                }
            },
        })
    }

    return (
        <Contenedor className="max-w-[35rem] sm:pt-8">
            <Tarjeta className="space-y-6 px-8 py-8">
                <div className="space-y-5 text-center">
                    <Logo className="mx-auto size-12 rounded-xl" />
                    <h1 className="text-[1.75rem] font-bold">Crea tu cuenta</h1>
                </div>
                <form
                    onSubmit={handleSubmit(enviar)}
                    noValidate
                    className="grid gap-x-4 gap-y-4 sm:grid-cols-2"
                >
                    <Campo
                        etiqueta="Nombre de usuario"
                        autoComplete="username"
                        className="sm:col-span-2"
                        error={errors.username?.message}
                        {...register('username')}
                    />
                    <Campo
                        etiqueta="Correo electrónico"
                        type="email"
                        autoComplete="email"
                        className="sm:col-span-2"
                        error={errors.email?.message}
                        {...register('email')}
                    />
                    <Campo
                        etiqueta="Nombres"
                        autoComplete="given-name"
                        error={errors.firstName?.message}
                        {...register('firstName')}
                    />
                    <Campo
                        etiqueta="Apellidos"
                        autoComplete="family-name"
                        error={errors.lastName?.message}
                        {...register('lastName')}
                    />
                    <Campo
                        etiqueta="Fecha de nacimiento"
                        type="date"
                        className="sm:col-span-2"
                        error={errors.birthDate?.message}
                        {...register('birthDate')}
                    />
                    <div className="space-y-3 sm:col-span-2">
                        <Campo
                            etiqueta="Contraseña (mín. 12 caracteres)"
                            type="password"
                            autoComplete="new-password"
                            ayuda="Entre 12 caracteres y 72 bytes."
                            error={errors.password?.message}
                            {...register('password')}
                        />
                        <MedidorFortaleza password={password ?? ''} />
                    </div>
                    {avisoGeneral && (
                        <Aviso tono="error" className="sm:col-span-2">
                            {avisoGeneral}
                        </Aviso>
                    )}
                    <Boton type="submit" cargando={registrar.isPending} className="mt-1 sm:col-span-2">
                        Crear cuenta
                    </Boton>
                </form>
                <p className="text-center text-[15px] text-slate-600 dark:text-slate-400">
                    ¿Ya tienes cuenta?{' '}
                    <Link to={RUTAS.login} className="text-marca-700 font-semibold dark:text-indigo-300">
                        Inicia sesión
                    </Link>
                </p>
            </Tarjeta>
        </Contenedor>
    )
}
