# Entendimiento del servicio

`banquito-switch-billing-service` calcula la comision del lote y coordina el cobro delegado al Core Bancario.

## Nota de migracion vigente

El ajuste de sobrante descrito en el flujo actual queda deprecado. La nueva regla es:

```text
Si una linea falla despues del fondeo, el dinero no se devuelve a la empresa.
```

El servicio debe migrar a Core REST via Kong para el cobro de comision:

```http
POST /api/v1/switch-core/payment-reservations/{reservationUuid}/service-fee-charge
```

`funding_adjustment` y los campos de release se conservan solo como legado de compatibilidad documental/persistente. No se escriben ni se usan en el flujo vigente.

## Responsabilidad en lenguaje simple

Cuando reporting confirma que todas las lineas del lote terminaron, billing calcula la comision sobre lineas cobrables, pide al Core que la cobre por REST via Kong y publica un evento con la trazabilidad para reporting final. Si existe `remainingAmount`, se conserva solo como monto no procesado/no devuelto.

## Flujo interno principal

1. `BatchLinesCompletedListener` recibe `BatchLinesCompletedEvent`.
2. `BillingServiceImpl` valida idempotencia por `batchId`.
3. Persiste el snapshot del lote en `batch_billing`.
4. Registra `remainingAmount` solo como metrica informativa y no llama release/reverse.
5. Calcula `commissionSubtotal = billableLines * unitFee`.
6. Persiste `commission_calculation`.
7. Llama al Core con `POST /service-fee-charge`.
8. Persiste `billing_core_request` con respuesta o falla controlada.
9. Publica `BillingCompletedEvent` solo una vez.

## Paquetes importantes

- `listener`: entrada RabbitMQ.
- `service` y `service.impl`: orquestacion de comision e interfaz de publicacion.
- `client`: cliente REST del Core Bancario via Kong.
- `config`: configuracion RabbitMQ, RestClient Core/Kong y canal gRPC legacy.
- `dto.event`: eventos consumidos/publicados.
- `dto.request` y `dto.response`: DTOs para encapsular requests/responses hacia el Core.
- `mapper`: conversion manual entre eventos, entidades y eventos de salida.
- `model`: entidades JPA propias.
- `repository`: repositorios Spring Data JPA propios.
- `enums`: estados locales de billing, request al Core y ajuste de fondeo.

## Clases principales

- `BatchLinesCompletedListener`: recibe el evento de cierre operativo de lineas.
- `BillingServiceImpl`: calcula comision, invoca Core/Kong, persiste resultado y publica evento final.
- `CoreBankingClient`: ejecuta `POST /api/v1/switch-core/payment-reservations/{reservationUuid}/service-fee-charge` por REST.
- `RabbitBillingCompletedEventPublisher`: publica `BillingCompletedEvent`.
- `BillingMapper`: arma entidades y eventos sin acceder a repositorios ni services.

## Tablas propias

- `batch_billing`: snapshot consolidado del lote, subtotal de comision, respuesta del Core y bandera de publicacion.
- `funding_adjustment`: tabla legacy/deprecada de ajuste de sobrante; ya no se escribe en el flujo vigente.
- `commission_calculation`: evidencia del calculo aplicado.
- `billing_core_request`: request idempotente de comision enviado al Core y respuesta recibida.

No existen foreign keys hacia otros microservicios ni hacia el Core.

## Eventos propios

Consume:

- `BatchLinesCompletedEvent` desde `switch.billing.batch-completed.queue`.

Publica:

- `BillingCompletedEvent` con routing key `billing.completed`.

## Integraciones externas

Consume Core Bancario por REST via Kong usando:

```http
POST /api/v1/switch-core/payment-reservations/{reservationUuid}/service-fee-charge
```

`coreFundingId` es un nombre legacy/transicional y su valor se usa como `reservationUuid`. El request envia `commissionSubtotal`, `correlationId` y `externalReference=COMMISSION-{batchId}`.

Core calcula IVA y total cobrado. El servicio lee `chargedCommissionTaxAmount`, `chargedCommissionTotalAmount`, `feeTransactionUuid` y `feeJournalEntryUuid`, con fallback a nombres legacy si Core los devuelve.

## Que cosas NO hace

- No expone endpoints REST publicos.
- No afecta saldos.
- No calcula IVA.
- No registra contabilidad.
- No libera, reversa ni ajusta sobrante hacia la empresa.
- No procesa lineas individuales.
- No genera reporte de novedades, comprobante corporativo ni notificaciones.

## Pendientes o limitaciones conocidas

- Agregar DLQ y politica de reintentos controlados para RabbitMQ.
- Reemplazar los fallbacks de compatibilidad cuando no existan mensajes legacy sin `companyRuc` o `sourceAccountNumber`.
- El token tecnico real se obtiene por `POST /api/v1/auth/client-token` y se cachea localmente; `CORE_KONG_AUTH_TOKEN` queda como override manual.
- Renombrar `coreFundingId` a `reservationUuid` en eventos, tablas y DTOs en una fase posterior.
- La integracion legacy Switch-Core por gRPC y `core-banking-mock` ya fueron retirados. Los DTOs de release y la tabla `funding_adjustment` permanecen como limpieza futura de sobrante legacy.
- El estado Core `CONSUMIDA_TOTAL` se interpreta como cobro exitoso y produce `COBRO_COMISION_EXITOSO`; fue validado con lote `b60f3d73-e773-403d-a9c0-94abdb3621bf` y reserva `475f8323-df27-4df8-ad49-6735e2a37698`.
