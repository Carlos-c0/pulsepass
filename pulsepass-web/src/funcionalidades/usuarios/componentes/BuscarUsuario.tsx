import { useState } from 'react'
import Badge from '../../../componentes/Badge'
import Boton from '../../../componentes/Boton'
import Campo from '../../../componentes/Campo'
import Cargando from '../../../componentes/Cargando'
import EstadoError from '../../../componentes/EstadoError'
import Tarjeta from '../../../componentes/Tarjeta'
import { useUsuarioPorUsername } from '../hooks'

export default function BuscarUsuario() {
    const [texto, setTexto] = useState('')
    const [username, setUsername] = useState('')
    const usuario = useUsuarioPorUsername(username)

    return (
        <Tarjeta className="space-y-4">
            <h2 className="text-lg font-semibold">Consultar una cuenta</h2>
            <form
                className="flex flex-col gap-3 sm:flex-row sm:items-end"
                onSubmit={(e) => {
                    e.preventDefault()
                    setUsername(texto.trim())
                }}
            >
                <Campo
                    etiqueta="Nombre de usuario"
                    value={texto}
                    onChange={(e) => setTexto(e.target.value)}
                    className="flex-1"
                />
                <Boton type="submit" disabled={!texto.trim()}>
                    Buscar
                </Boton>
            </form>
            {username && usuario.isPending && <Cargando filas={1} />}
            {usuario.isError && <EstadoError error={usuario.error} />}
            {usuario.data && (
                <p className="flex flex-wrap items-center gap-2 text-sm">
                    <strong>@{usuario.data.username}</strong> · {usuario.data.email}
                    <Badge tono={usuario.data.active ? 'verde' : 'gris'}>
                        {usuario.data.active ? 'Activa' : 'Inactiva'}
                    </Badge>
                </p>
            )}
        </Tarjeta>
    )
}
