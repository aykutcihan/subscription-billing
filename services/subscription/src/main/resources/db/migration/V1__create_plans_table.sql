CREATE TABLE plans (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    billing_cycle VARCHAR(20)  NOT NULL,
    price         NUMERIC(10, 2) NOT NULL
);

INSERT INTO plans (name, billing_cycle, price) VALUES
    ('Basic',   'MONTHLY',  99.00),
    ('Premium', 'MONTHLY', 199.00),
    ('Yearly',  'YEARLY',  999.00);
