import { isRouteErrorResponse, useRouteError } from 'react-router'
import Boton from '../componentes/Boton'
import Contenedor from '../componentes/Contenedor'
import EnlaceBoton from '../componentes/EnlaceBoton'
import Tarjeta from '../componentes/Tarjeta'
import Navbar from '../layouts/Navbar'
import { RUTAS } from '../lib/rutas'

// errorElement del layout raiz: nunca muestra el stack trace ni el mensaje tecnico
export default function ErrorPagina() {
    const error = useRouteError()
    const mensaje =
        isRouteErrorResponse(error) && error.status === 404
            ? 'No encontramos esta página.'
            : 'Ocurrió un error inesperado. Inténtalo más tarde.'

    return (
        <div className="min-h-dvh">
            <Navbar />
            <main>
                <Contenedor>
                    <Tarjeta className="space-y-4 px-8 py-8">
                        <h1 className="text-[1.6rem] font-bold">¡Ups! Algo salió mal.</h1>
                        <p className="text-slate-600 dark:text-slate-400">{mensaje}</p>
                        <div className="flex flex-wrap gap-3">
                            <Boton onClick={() => window.location.reload()}>Reintentar</Boton>
                            <EnlaceBoton to={RUTAS.inicio} variante="secundario">
                                Volver al inicio
                            </EnlaceBoton>
                        </div>
                    </Tarjeta>
                </Contenedor>
            </main>
        </div>
    )
}
