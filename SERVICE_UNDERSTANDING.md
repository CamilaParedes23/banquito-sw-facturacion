# Entendimiento del servicio

`banquito-switch-billing-service` ajusta el sobrante del fondeo global, calcula la comision del lote y coordina el cobro delegado al Core Bancario.

## Responsabilidad en lenguaje simple

Cuando reporting confirma que todas las lineas del lote terminaron, billing revisa si sobro dinero del fondeo global. Si hay sobrante, pide al Core que lo libere o reverse. Despues calcula la comision sobre lineas cobrables, pide al Core que la cobre y publica un evento con la trazabilidad para reporting final.

## Flujo interno principal

1. `BatchLinesCompletedListener` recibe `BatchLinesCompletedEvent`.
2. `BillingServiceImpl` valida idempotencia por `batchId`.
3. Persiste el snapshot del lote en `batch_billing`.
4. Procesa ajuste de sobrante en `funding_adjustment`.
5. Si el ajuste aplica y falla, no cobra comision.
6. Calcula `commissionSubtotal = billableLines * unitFee`.
7. Persiste `commission_calculation`.
8. Llama al Core con `RequestCommissionCharge`.
9. Persiste `billing_core_request` con respuesta o falla controlada.
10. Publica `BillingCompletedEvent` solo una vez.

## Paquetes importantes

- `listener`: entrada RabbitMQ.
- `service` y `service.impl`: orquestacion de ajuste, comision e interfaz de publicacion.
- `client`: cliente gRPC del Core Bancario.
- `config`: canal gRPC y configuracion RabbitMQ.
- `dto.event`: eventos consumidos/publicados.
- `dto.request` y `dto.response`: DTOs para encapsular requests/responses hacia el Core.
- `mapper`: conversion manual entre eventos, entidades y eventos de salida.
- `model`: entidades JPA propias.
- `repository`: repositorios Spring Data JPA propios.
- `enums`: estados locales de billing, request al Core y ajuste de fondeo.

## Clases principales

- `BatchLinesCompletedListener`: recibe el evento de cierre operativo de lineas.
- `BillingServiceImpl`: ajusta sobrante, calcula comision, invoca Core, persiste resultado y publica evento final.
- `CoreBankingClient`: ejecuta `ReleaseFunding` y `RequestCommissionCharge` por gRPC.
- `RabbitBillingCompletedEventPublisher`: publica `BillingCompletedEvent`.
- `BillingMapper`: arma entidades y eventos sin acceder a repositorios ni services.

## Tablas propias

- `batch_billing`: snapshot consolidado del lote, subtotal de comision, respuesta del Core y bandera de publicacion.
- `funding_adjustment`: ajuste de sobrante del fondeo global con idempotencia `RELEASE-{batchId}`.
- `commission_calculation`: evidencia del calculo aplicado.
- `billing_core_request`: request idempotente de comision enviado al Core y respuesta recibida.

No existen foreign keys hacia otros microservicios ni hacia el Core.

## Eventos propios

Consume:

- `BatchLinesCompletedEvent` desde `switch.billing.batch-completed.queue`.

Publica:

- `BillingCompletedEvent` con routing key `billing.completed`.

## Integraciones externas

Consume el mock/Core Bancario por gRPC usando:

- `CoreBankingService.ReleaseFunding`
- `CoreBankingService.RequestCommissionCharge`

## Que cosas NO hace

- No expone endpoints REST publicos.
- No afecta saldos.
- No calcula IVA.
- No registra contabilidad.
- No procesa lineas individuales.
- No genera reporte de novedades, comprobante corporativo ni notificaciones.

## Pendientes o limitaciones conocidas

- Agregar DLQ y politica de reintentos controlados para RabbitMQ.
- Reemplazar los fallbacks de compatibilidad cuando no existan mensajes legacy sin `companyRuc` o `sourceAccountNumber`.
- Integrar `BillingCompletedEvent` con la fase posterior de reporting final.
- Implementar reporting final, comprobantes y notificaciones en la siguiente fase.
