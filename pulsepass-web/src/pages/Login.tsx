import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Link, useLocation } from 'react-router'
import Aviso from '../componentes/Aviso'
import Boton from '../componentes/Boton'
import Campo from '../componentes/Campo'
import Contenedor from '../componentes/Contenedor'
import { Logo } from '../componentes/Iconos'
import Tarjeta from '../componentes/Tarjeta'
import { useLogin } from '../funcionalidades/auth/hooks'
import { LoginSchema, type LoginForm } from '../funcionalidades/auth/tipos'
import { esEstado, mensajeDeError } from '../lib/errores'
import { leerEstadoNavegacion } from '../lib/estadoNavegacion'
import { RUTAS } from '../lib/rutas'

export default function Login() {
    const { motivo, registrado } = leerEstadoNavegacion(useLocation().state)
    const login = useLogin()
    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<LoginForm>({ resolver: zodResolver(LoginSchema) })

    // Al quedar autenticado, SoloAnonimo redirige a la ruta de origen
    const enviar = (datos: LoginForm) => login.mutate(datos)

    return (
        <Contenedor className="max-w-[26.5rem] sm:pt-8">
            <Tarjeta className="space-y-6 px-8 py-8">
                <div className="space-y-5 text-center">
                    <Logo className="mx-auto size-12 rounded-xl" />
                    <h1 className="text-[1.75rem] font-bold">Inicia sesión</h1>
                </div>

                {registrado && <Aviso tono="exito">Cuenta creada, inicia sesión.</Aviso>}
                {motivo === 'expirada' && (
                    <Aviso tono="info">Tu sesión expiró. Inicia sesión de nuevo.</Aviso>
                )}

                <form onSubmit={handleSubmit(enviar)} noValidate className="space-y-4">
                    <Campo
                        etiqueta="Usuario o correo"
                        autoComplete="username"
                        error={errors.identifier?.message}
                        {...register('identifier')}
                    />
                    <Campo
                        etiqueta="Contraseña"
                        type="password"
                        autoComplete="current-password"
                        error={errors.password?.message}
                        {...register('password')}
                    />
                    {login.isError && (
                        <Aviso tono="error">
                            {/* Nunca se distingue si fallo el usuario o la contrasena */}
                            {esEstado(login.error, 401) ? 'Credenciales inválidas.' : mensajeDeError(login.error)}
                        </Aviso>
                    )}
                    <Boton type="submit" cargando={login.isPending} className="w-full">
                        Ingresar
                    </Boton>
                </form>

                <p className="text-center text-[15px] text-slate-600 dark:text-slate-400">
                    ¿No tienes cuenta?{' '}
                    <Link to={RUTAS.registro} className="text-marca-700 font-semibold dark:text-indigo-300">
                        Regístrate
                    </Link>
                </p>
            </Tarjeta>
        </Contenedor>
    )
}
