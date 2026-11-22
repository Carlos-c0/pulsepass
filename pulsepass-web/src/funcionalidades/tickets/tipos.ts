import { z } from 'zod'

export const TicketTypeSchema = z.enum(['GENERAL', 'VIP', 'BACKSTAGE', 'STUDENT'])
export const TicketStatusSchema = z.enum(['RESERVED', 'PAID', 'CANCELLED', 'USED'])

export type TipoTicket = z.infer<typeof TicketTypeSchema>
export type EstadoTicket = z.infer<typeof TicketStatusSchema>

export const TicketResponseSchema = z.object({
    ticketCode: z.string(),
    type: TicketTypeSchema,
    // BigDecimal puede llegar como numero o como texto segun el serializador
    price: z.coerce.number(),
    status: TicketStatusSchema,
    purchaseDate: z.string(),
    userEmail: z.string(),
    eventCode: z.string(),
    eventName: z.string(),
})

export type Ticket = z.infer<typeof TicketResponseSchema>

export const CompraSchema = z.object({ type: TicketTypeSchema })
export type CompraForm = z.infer<typeof CompraSchema>

export const BuscarTicketSchema = z.object({
    ticketCode: z.string().trim().min(1, 'Ingresa el código del ticket'),
})
export type BuscarTicketForm = z.infer<typeof BuscarTicketSchema>
