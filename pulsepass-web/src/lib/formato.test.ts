import { describe, expect, it } from 'vitest'
import {
    formatearFecha,
    formatearFechaCorta,
    formatearFechaHora,
    formatearFechaNumerica,
    formatearMomento,
    formatearPrecio,
} from './formato'

describe('lib/formato', () => {
    it('TEST-FE-LIB-005: formatea 250000 como pesos colombianos', () => {
        const texto = formatearPrecio(250000).replace(/\s/g, ' ')
        expect(texto).toBe('$ 250.000')
    })

    it('formatea las fechas como en el sistema visual', () => {
        expect(formatearFecha('2026-12-04T21:00:00')).toBe('4 dic 2026')
        expect(formatearFechaCorta('2026-11-05T14:32:00')).toBe('05 nov 2026')
        expect(formatearFechaHora('2026-12-12T20:00:00')).toBe('12 dic 2026 · 20:00')
        expect(formatearMomento('2026-11-05T14:32:00')).toBe('05 nov 2026 14:32')
    })

    it('una fecha sin hora no se corre un día por la zona horaria', () => {
        expect(formatearFechaNumerica('1998-04-12')).toBe('12/04/1998')
    })
})
