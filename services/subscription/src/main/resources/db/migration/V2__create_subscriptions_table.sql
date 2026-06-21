CREATE TABLE subscriptions (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT       NOT NULL,
    plan_id           BIGINT       NOT NULL REFERENCES plans(id),
    status            VARCHAR(20)  NOT NULL,
    started_at        DATE         NOT NULL,
    next_billing_date DATE         NOT NULL
);
