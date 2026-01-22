create table if not exists bookings (
  booking_id uuid primary key,
  correlation_id uuid not null,
  flight_id varchar(50) not null,
  seat_class varchar(20) not null,
  amount numeric(12,2) not null,
  currency varchar(3) not null,
  status varchar(30) not null,
  created_at timestamptz not null,
  updated_at timestamptz not null
);