-- Loyalty Accruals table for idempotency
create table if not exists loyalty_accruals (
  booking_id uuid primary key,
  member_id uuid not null,
  points_credited integer not null,
  amount numeric(10,2) not null,
  currency varchar(10) not null,
  correlation_id uuid,
  created_at timestamptz not null,
  constraint fk_loyalty_accrual_member foreign key (member_id) references loyalty_members(member_id)
);

-- Index for fast lookup by member_id
create index idx_loyalty_accrual_member on loyalty_accruals(member_id);
