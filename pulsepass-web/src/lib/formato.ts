// Formatos del sistema visual (9.1): "12 dic 2026", "12 dic 2026 · 20:00", "$ 250.000".
// Se arman a mano porque Intl en es-CO agrega "de" y "p. m." y el diseño no los usa.
const MESES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic']

const pesos = new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    maximumFractionDigits: 0,
})

const dosDigitos = (n: number) => String(n).padStart(2, '0')

function partes(valor: string) {
    const d = leerFecha(valor)
    return {
        dia: d.getDate(),
        mes: MESES[d.getMonth()] ?? '',
        anio: d.getFullYear(),
        hora: `${dosDigitos(d.getHours())}:${dosDigitos(d.getMinutes())}`,
        mesNumero: d.getMonth() + 1,
    }
}

// Tarjetas de evento: "4 dic 2026"
export function formatearFecha(valor: string): string {
    const p = partes(valor)
    return `${p.dia} ${p.mes} ${p.anio}`
}

// Listas de tickets: "05 nov 2026"
export function formatearFechaCorta(valor: string): string {
    const p = partes(valor)
    return `${dosDigitos(p.dia)} ${p.mes} ${p.anio}`
}

// Detalle de evento: "12 dic 2026 · 20:00"
export function formatearFechaHora(valor: string, separador = ' · '): string {
    const p = partes(valor)
    return `${p.dia} ${p.mes} ${p.anio}${separador}${p.hora}`
}

// Fecha de compra: "05 nov 2026 14:32"
export function formatearMomento(valor: string): string {
    const p = partes(valor)
    return `${dosDigitos(p.dia)} ${p.mes} ${p.anio} ${p.hora}`
}

// Fecha de nacimiento: "12/04/1998"
export function formatearFechaNumerica(valor: string): string {
    const p = partes(valor)
    return `${dosDigitos(p.dia)}/${dosDigitos(p.mesNumero)}/${p.anio}`
}

export function formatearPrecio(valor: number): string {
    return pesos.format(valor)
}

// El backend manda LocalDateTime sin zona ("2026-12-12T20:00:00"); new Date lo lee como hora local.
// "1998-04-12" en cambio se tomaria como medianoche UTC y en Colombia saldria el dia anterior,
// por eso una fecha sin hora se arma con sus partes.
function leerFecha(valor: string): Date {
    const soloFecha = /^(\d{4})-(\d{2})-(\d{2})$/.exec(valor)
    if (soloFecha) {
        const [, anio, mes, dia] = soloFecha
        return new Date(Number(anio), Number(mes) - 1, Number(dia))
    }
    return new Date(valor)
}
