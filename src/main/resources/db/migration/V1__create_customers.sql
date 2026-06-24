CREATE TABLE customers (
    customer_id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    mobile_number VARCHAR(32) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    version BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_customers_email UNIQUE (email),
    CONSTRAINT uk_customers_mobile_number UNIQUE (mobile_number),
    CONSTRAINT chk_customers_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'CLOSED'))
);