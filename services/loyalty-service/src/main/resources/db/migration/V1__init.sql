-- Loyalty Members table
create table if not exists loyalty_members (
  member_id uuid primary key,
  first_name varchar(100) not null,
  last_name varchar(100) not null,
  email varchar(255) not null,
  tier varchar(20) not null,
  status varchar(20) not null,
  points_balance integer not null default 0,
  created_at timestamptz not null,
  constraint uq_loyalty_email unique (email)
);

-- Index for fast lookup by email
create index idx_loyalty_email on loyalty_members(email);
