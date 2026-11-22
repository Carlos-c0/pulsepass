import Contenedor from './Contenedor'
import EnlaceBoton from './EnlaceBoton'
import Tarjeta from './Tarjeta'
import { RUTAS } from '../lib/rutas'

// 403 y 404 comparten forma: numero grande, mensaje y "Volver al inicio" (9.19)
export default function PaginaCodigo({ codigo, mensaje }: { codigo: string; mensaje: string }) {
    return (
        <Contenedor className="max-w-[34rem] sm:pt-12">
            <Tarjeta className="flex flex-col items-center gap-5 px-8 py-10 text-center">
                <p
                    aria-hidden="true"
                    className="font-titulo text-marca-700 text-6xl font-bold dark:text-indigo-300"
                >
                    {codigo}
                </p>
                <h1 className="font-cuerpo text-[17px] font-normal">{mensaje}</h1>
                <EnlaceBoton to={RUTAS.inicio}>Volver al inicio</EnlaceBoton>
            </Tarjeta>
        </Contenedor>
    )
}
