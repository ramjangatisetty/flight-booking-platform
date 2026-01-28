-- Payment Transactions table for idempotency
create table if not exists payment_transactions (
  payment_id uuid primary key,
  booking_id uuid not null,
  status varchar(20) not null,
  amount numeric(10,2) not null,
  currency varchar(10) not null,
  provider varchar(50) not null,
  correlation_id uuid,
  request_event_id varchar(255) not null,
  created_at timestamptz not null,
  constraint uq_payment_booking unique (booking_id),
  constraint uq_payment_request_event unique (request_event_id)
);

-- Index for fast lookup by booking_id
create index idx_payment_booking on payment_transactions(booking_id);

-- Index for fast lookup by request_event_id (primary deduplication key)
create index idx_payment_request_event on payment_transactions(request_event_id);
