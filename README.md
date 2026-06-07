# banquito-switch-billing-service

## Responsabilidad

Servicio responsable de ajustar el sobrante del fondeo global, calcular la comision del lote sobre lineas cobrables y solicitar al Core Bancario el cobro delegado mediante gRPC.

No expone endpoints REST publicos. Su entrada funcional es el evento `BatchLinesCompletedEvent`.

## Flujo implementado

1. Consume `BatchLinesCompletedEvent` desde `switch.billing.batch-completed.queue`.
2. Registra el snapshot consolidado del lote en `batch_billing`.
3. Evalua `remainingAmount`.
4. Si `remainingAmount > 0`, solicita `ReleaseFunding` al Core con `idempotencyKey=RELEASE-{batchId}`.
5. Registra el ajuste en `funding_adjustment`.
6. Calcula `commissionSubtotal = billableLines * switch.billing.unit-fee`.
7. Registra el calculo en `commission_calculation`.
8. Solicita `RequestCommissionCharge` al Core con `idempotencyKey=COMMISSION-{batchId}`.
9. Registra la respuesta del Core en `billing_core_request` y `batch_billing`.
10. Publica `BillingCompletedEvent` con estado de ajuste y comision.

Si `remainingAmount = 0`, registra `NO_APLICA` y no llama `ReleaseFunding`. Si `remainingAmount < 0` o falta `coreFundingId` para liberar sobrante, registra `FALLIDO` y no avanza al cobro de comision.

## Ejecucion local

Compilar sin Maven global desde la raiz del workspace:

```powershell
docker run --rm -v maven_repo:/root/.m2 -v "${PWD}\banquito-switch-billing-service:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-21 mvn -o -DskipTests compile
```

Construir imagen Docker desde la raiz:

```powershell
docker compose build billing-service
```

Puerto por defecto: `8084`. Solo se usa para actuator/health.

## Variables de entorno

- `SERVER_PORT`
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD`
- `RABBIT_EXCHANGE_BILLING`
- `RABBIT_QUEUE_BILLING_BATCH_COMPLETED`
- `RABBIT_QUEUE_REPORTING_BILLING_COMPLETED`
- `RABBIT_ROUTING_KEY_BATCH_LINES_COMPLETED`
- `RABBIT_ROUTING_KEY_BILLING_COMPLETED`
- `SWITCH_BILLING_UNIT_FEE`
- `SWITCH_BILLING_FALLBACK_COMPANY_RUC`
- `SWITCH_BILLING_FALLBACK_SOURCE_ACCOUNT_NUMBER`
- `CORE_GRPC_HOST`, `CORE_GRPC_PORT`, `CORE_GRPC_DEADLINE_MS`

Los fallback de empresa/cuenta solo cubren mensajes legacy sin esos campos. No existe fallback para `coreFundingId`; si falta, el ajuste de sobrante queda `FALLIDO`.

## Base de datos

Usa `switch_billing_db`. Hibernate esta configurado con `spring.jpa.hibernate.ddl-auto=validate`; las tablas se crean desde `src/main/resources/db/init/001_create_billing_tables.sql`.

Tablas propias:

- `batch_billing`
- `funding_adjustment`
- `commission_calculation`
- `billing_core_request`

## Eventos

Consume:

- Exchange: `switch.billing.exchange`
- Queue: `switch.billing.batch-completed.queue`
- Routing key: `batch.lines.completed`
- Evento: `BatchLinesCompletedEvent`

Publica:

- Exchange: `switch.billing.exchange`
- Routing key: `billing.completed`
- Cola observable: `switch.reporting.billing-completed.queue`
- Evento: `BillingCompletedEvent`

`BillingCompletedEvent` incluye `remainingAmount`, `fundingAdjustmentStatus`, `releasedAmount` y `fundingReleaseCoreTransactionId` para que reporting final no consulte la base de billing.

No existe DLQ configurada en esta fase; queda pendiente para endurecimiento.

## Integracion Core

El client gRPC `client/CoreBankingClient.java` consume:

- `CoreBankingService.ReleaseFunding`
- `CoreBankingService.RequestCommissionCharge`

El Core libera/reversa sobrantes, calcula IVA, total cobrado y contabilidad. El Switch solo registra referencias y estados devueltos.

## Prueba basica

Levantar el flujo hasta billing:

```powershell
docker compose up -d postgres rabbitmq core-banking-mock batch-service routing-service on-us-settlement-service reporting-service billing-service
```

Cargar `docs/examples/files/valid_mixed_batch.csv` en `batch-service`. Al finalizar las lineas, billing debe registrar `NO_APLICA` para ajuste de sobrante, cobrar comision y publicar `BillingCompletedEvent`.

Verificar:

```powershell
docker exec banquito-switch-postgres psql -U postgres -d switch_billing_db -c "select batch_id, status, billable_lines, unit_fee, commission_subtotal, core_commission_charge_id, total_charged_amount from batch_billing order by received_at desc limit 5;"
docker exec banquito-switch-postgres psql -U postgres -d switch_billing_db -c "select batch_id, status, remaining_amount, released_amount, core_transaction_id from funding_adjustment order by created_at desc limit 5;"
```
