CREATE TABLE IF NOT EXISTS batch_billing (
    billing_id UUID PRIMARY KEY,
    source_event_id UUID NOT NULL,
    batch_id UUID NOT NULL UNIQUE,
    correlation_id UUID NOT NULL,
    company_ruc VARCHAR(20),
    source_account_number VARCHAR(40),
    core_funding_id VARCHAR(80),
    total_lines INTEGER NOT NULL,
    on_us_credited_lines INTEGER NOT NULL,
    off_us_included_lines INTEGER NOT NULL,
    rejected_lines INTEGER NOT NULL,
    failed_lines INTEGER NOT NULL,
    billable_lines INTEGER NOT NULL,
    control_amount NUMERIC(18, 2),
    processed_amount NUMERIC(18, 2) NOT NULL,
    remaining_amount NUMERIC(18, 2),
    currency VARCHAR(3) NOT NULL,
    unit_fee NUMERIC(18, 2) NOT NULL,
    commission_subtotal NUMERIC(18, 2) NOT NULL,
    status VARCHAR(40) NOT NULL,
    core_response_status VARCHAR(40),
    core_commission_charge_id VARCHAR(80),
    core_transaction_id VARCHAR(80),
    tax_amount NUMERIC(18, 2),
    total_charged_amount NUMERIC(18, 2),
    core_response_message VARCHAR(500),
    billing_completed_event_published BOOLEAN NOT NULL,
    billing_completed_event_id UUID,
    received_at TIMESTAMPTZ NOT NULL,
    calculated_at TIMESTAMPTZ,
    core_requested_at TIMESTAMPTZ,
    core_responded_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ NOT NULL
);

ALTER TABLE batch_billing
    ADD COLUMN IF NOT EXISTS core_funding_id VARCHAR(80);

CREATE TABLE IF NOT EXISTS commission_calculation (
    commission_calculation_id UUID PRIMARY KEY,
    billing_id UUID NOT NULL,
    batch_id UUID NOT NULL UNIQUE,
    billable_lines INTEGER NOT NULL,
    unit_fee NUMERIC(18, 2) NOT NULL,
    commission_subtotal NUMERIC(18, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    calculated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS billing_core_request (
    billing_core_request_id UUID PRIMARY KEY,
    billing_id UUID NOT NULL,
    batch_id UUID NOT NULL UNIQUE,
    idempotency_key VARCHAR(80) NOT NULL UNIQUE,
    request_status VARCHAR(40) NOT NULL,
    requested_amount NUMERIC(18, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    core_response_status VARCHAR(40),
    core_commission_charge_id VARCHAR(80),
    core_transaction_id VARCHAR(80),
    tax_amount NUMERIC(18, 2),
    total_charged_amount NUMERIC(18, 2),
    core_response_message VARCHAR(500),
    requested_at TIMESTAMPTZ NOT NULL,
    responded_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS funding_adjustment (
    funding_adjustment_id UUID PRIMARY KEY,
    billing_id UUID NOT NULL,
    batch_id UUID NOT NULL UNIQUE,
    core_funding_id VARCHAR(80),
    remaining_amount NUMERIC(18, 2) NOT NULL,
    released_amount NUMERIC(18, 2),
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(40) NOT NULL,
    core_transaction_id VARCHAR(80),
    core_response_status VARCHAR(40),
    core_response_message VARCHAR(500),
    idempotency_key VARCHAR(80) NOT NULL UNIQUE,
    requested_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_batch_billing_batch_id
    ON batch_billing (batch_id);

CREATE INDEX IF NOT EXISTS idx_batch_billing_status
    ON batch_billing (status);

CREATE INDEX IF NOT EXISTS idx_commission_calculation_batch_id
    ON commission_calculation (batch_id);

CREATE INDEX IF NOT EXISTS idx_billing_core_request_batch_id
    ON billing_core_request (batch_id);

CREATE INDEX IF NOT EXISTS idx_funding_adjustment_batch_id
    ON funding_adjustment (batch_id);

CREATE INDEX IF NOT EXISTS idx_funding_adjustment_status
    ON funding_adjustment (status);
