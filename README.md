# banquito-switch-billing-service

## Nota de arquitectura objetivo

Este servicio ya consume Core REST via Kong/API Manager para el cobro de comision.

Operacion objetivo para comision:

```http
POST http://localhost:8000/api/v1/switch-core/payment-reservations/{reservationUuid}/service-fee-charge
```

`externalReference` debe ser estable, por ejemplo `COMMISSION-{batchId}`.

La regla vigente elimina liberacion, reverso o ajuste de sobrante hacia la empresa. `ReleaseFunding`, `RELEASE-{batchId}`, `"AJUSTE_FONDEO"`, `fundingAdjustmentStatus`, `releasedAmount` y `fundingReleaseCoreTransactionId` quedan deprecados. `remainingAmount` se conserva solo como metrica informativa de monto no procesado/no devuelto.

## Responsabilidad

Servicio responsable de calcular la comision del lote sobre lineas cobrables y solicitar al Core Bancario el cobro delegado mediante REST via Kong.

No expone endpoints REST publicos. Su entrada funcional es el evento `BatchLinesCompletedEvent`.

## Flujo implementado

1. Consume `BatchLinesCompletedEvent` desde `switch.billing.batch-completed.queue`.
2. Registra el snapshot consolidado del lote en `"COBRO_COMISION_LOTE"`.
3. Conserva `remainingAmount` solo como metrica informativa; no libera ni reversa sobrante.
4. Calcula la tarifa unitaria por tramos segun el volumen total de lineas cobrables y luego calcula `commissionSubtotal = billableLines * unitFee`.
5. Registra el calculo en `"CALCULO_COMISION"`.
6. Solicita `POST /service-fee-charge` al Core con `externalReference=COMMISSION-{batchId}`.
7. Registra la respuesta del Core en `"SOLICITUD_COBRO_CORE"` y `"COBRO_COMISION_LOTE"`.
8. Publica `BillingCompletedEvent` con campos legacy de liberacion en `null`.

Si falta `coreFundingId`/`reservationUuid`, el cobro de comision queda `COBRO_COMISION_FALLIDO` porque no se puede construir el path REST. Un `remainingAmount` positivo o negativo no bloquea el cobro.

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
- El tarifario es interno por tramos; `SWITCH_BILLING_UNIT_FEE` ya no debe usarse como tarifa fija operativa.
- `SWITCH_BILLING_FALLBACK_COMPANY_RUC`
- `SWITCH_BILLING_FALLBACK_SOURCE_ACCOUNT_NUMBER`
- `CORE_KONG_BASE_URL`
- `CORE_KONG_SWITCH_CORE_PATH`
- `CORE_KONG_PAYMENT_RESERVATIONS_PATH`
- `CORE_KONG_AUTH_TOKEN`
- `CORE_KONG_CLIENT_TOKEN_ENABLED`
- `CORE_KONG_CLIENT_ID`
- `CORE_KONG_CLIENT_SECRET`
- `CORE_KONG_REQUIRED_SCOPE`
- `CORE_KONG_CLIENT_TOKEN_PATH`
- `CORE_KONG_CLIENT_TOKEN_REFRESH_SKEW_SECONDS`
- `CORE_KONG_CONNECT_TIMEOUT_MS`
- `CORE_KONG_READ_TIMEOUT_MS`
- `CORE_SWITCH_DEFAULT_ACCOUNTING_DATE`

Los fallback de empresa/cuenta solo cubren mensajes legacy sin esos campos. No existe fallback para `coreFundingId`; su valor debe contener el `reservationUuid` real de Core.

## Base de datos

Usa `SWITCH_COMISIONES_DB`. Hibernate esta configurado con `spring.jpa.hibernate.ddl-auto=validate`; las tablas se crean desde `src/main/resources/db/init/001_create_billing_tables.sql`.

Tablas propias:

- `"COBRO_COMISION_LOTE"`
- `"AJUSTE_FONDEO"` (legacy/deprecada; ya no se escribe en el flujo vigente)
- `"CALCULO_COMISION"`
- `"SOLICITUD_COBRO_CORE"`

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

`BillingCompletedEvent` conserva `remainingAmount` como metrica informativa. Los campos legacy `fundingAdjustmentStatus`, `releasedAmount` y `fundingReleaseCoreTransactionId` se publican en `null` en el flujo vigente.

No existe DLQ configurada en esta fase; queda pendiente para endurecimiento.

## Integracion Core

El cliente `client/CoreBankingClient.java` consume Core REST via Kong:

```http
POST /api/v1/switch-core/payment-reservations/{reservationUuid}/service-fee-charge
```

Mapeo:

- `coreFundingId` legacy se usa como `{reservationUuid}`.
- `commissionSubtotal` se envia como `amount`.
- `correlationId` se envia desde el evento.
- `externalReference` usa `COMMISSION-{batchId}`.
- `accountingDate` usa `CORE_SWITCH_DEFAULT_ACCOUNTING_DATE`; si esta vacia, usa la fecha local del servicio.

Core calcula IVA y contabilidad internamente, pero el `ReservationResponse` actual no devuelve IVA, total cobrado ni `transactionUuid` de comision. Por eso `taxAmount`, `totalChargedAmount`, `coreTransactionId` y `coreCommissionChargeId` pueden quedar `null`.

Estados Core considerados exitosos para el cobro:

- `APPROVED`
- `ACTIVA`
- `CONSUMIDA`
- `CONSUMIDA_PARCIAL`
- `CONSUMIDA_TOTAL`

En la integracion REST actual, `CONSUMIDA_TOTAL` puede llegar despues de cobrar la comision sobre una reserva ya consumida por lineas On-Us; debe tratarse como cobro aceptado mientras la llamada HTTP sea exitosa.

Autenticacion transicional:

```text
CORE_KONG_AUTH_TOKEN
CORE_KONG_CLIENT_TOKEN_ENABLED
CORE_KONG_CLIENT_ID
CORE_KONG_CLIENT_SECRET
CORE_KONG_REQUIRED_SCOPE
CORE_KONG_CLIENT_TOKEN_PATH
CORE_KONG_CLIENT_TOKEN_REFRESH_SKEW_SECONDS
```

Si `CORE_KONG_AUTH_TOKEN` esta configurado, se usa como override manual. Si esta vacio y `CORE_KONG_CLIENT_TOKEN_ENABLED=true`, el servicio obtiene `client-token` por `POST /api/v1/auth/client-token`, lo cachea en memoria y lo renueva antes de expirar.

## Prueba basica

Levantar el flujo hasta billing:

```powershell
docker compose up -d postgres rabbitmq batch-service routing-service on-us-settlement-service reporting-service billing-service
```

Cargar `docs/examples/files/valid_mixed_batch.csv` en `batch-service`. Al finalizar las lineas, billing debe registrar `NO_APLICA` para ajuste de sobrante, cobrar comision y publicar `BillingCompletedEvent`.

Tarifario vigente:

| Lineas cobrables | Tarifa unitaria |
| ---: | ---: |
| 1 a 10 | 0.50 |
| 11 a 100 | 0.40 |
| 101 a 500 | 0.30 |
| 501 a 1000 | 0.20 |
| 1001 a 10000 | 0.10 |
| 10001 en adelante | 0.05 |

Verificar:

```powershell
docker exec banquito-switch-postgres psql -U postgres -d SWITCH_COMISIONES_DB -c 'select "ID_LOTE", "ESTADO", "LINEAS_COBRABLES", "TARIFA_UNITARIA", "SUBTOTAL_COMISION", "ID_COBRO_COMISION_CORE", "MONTO_TOTAL_COBRADO" from "COBRO_COMISION_LOTE" order by "FECHA_RECEPCION" desc limit 5;'
docker exec banquito-switch-postgres psql -U postgres -d SWITCH_COMISIONES_DB -c 'select "ID_LOTE", "MONTO_REMANENTE", "ESTADO", "ESTADO_RESPUESTA_CORE", "MENSAJE_RESPUESTA_CORE" from "COBRO_COMISION_LOTE" order by "FECHA_RECEPCION" desc limit 5;'
```

Para probar contra Core real por Kong, configurar:

```powershell
$env:CORE_KONG_BASE_URL="http://localhost:8000"
$env:CORE_KONG_CLIENT_SECRET="<secret-tecnico>"
$env:CORE_SWITCH_DEFAULT_ACCOUNTING_DATE="2026-06-05"
```

Si el servicio corre dentro de Docker Compose junto al stack Core, usar la red externa compartida `banquito-net` y configurar `CORE_KONG_BASE_URL=http://kong-gateway:8000`. `host.docker.internal:8000` queda solo como alternativa local si no se usa la red compartida.

## Evidencia de validacion con Core real

Validacion ejecutada contra Kong/Core real usando token inyectado en `CORE_KONG_AUTH_TOKEN`. Desde la fase de token automatico, `CORE_KONG_AUTH_TOKEN` queda como override manual y el camino normal usa `client-token` cacheado.

- Token tecnico obtenido por `POST /api/v1/auth/client-token` con cliente demo `switch-pagos-internos-service` y scope solicitado `core.reserve.consume`. En los seeds revisados no existe scope especifico para comision; el JWT emitido incluyo `core.reserve.consume`, `core.reserve.create` y `core.reserve.release`.
- Lote `6b5ed5e6-dd9f-4957-8e81-7f64affd88a3`, reserva `bd057bec-abe1-4976-890e-2a7c5c32b50a`, llego a Core por `service-fee-charge` y Core respondio `HTTP 200`, pero el servicio lo marco fallido porque aun no interpretaba `CONSUMIDA_TOTAL` como exito.
- Evidencia historica previa al tarifario por tramos: el lote `b60f3d73-e773-403d-a9c0-94abdb3621bf`, reserva `475f8323-df27-4df8-ad49-6735e2a37698`, una linea cobrable y `commissionSubtotal=0.40` recibio `HTTP 200` de Core con estado `CONSUMIDA_TOTAL`.
- Evidencia vigente con tarifario por tramos: el lote `82130617-641e-463c-9378-9b6b10fdc071`, una linea cobrable, genero `unitFee=0.50` y `commissionSubtotal=0.50`; el lote `c0d9357d-278b-4193-a6f8-1effec30ca9c`, once lineas cobrables, genero `unitFee=0.40` y `commissionSubtotal=4.40`.
- Resultado local: `COBRO_COMISION_EXITOSO`, request Core `APROBADO`, `BillingCompletedEvent` publicado y `remainingAmount=0.00` informativo sin release/reverse.
