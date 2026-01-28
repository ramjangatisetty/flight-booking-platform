-- Inventory Items table
create table if not exists inventory_items (
  id uuid primary key,
  flight_id varchar(50) not null,
  seat_class varchar(20) not null,
  available_seats integer not null,
  version bigint not null default 0,
  constraint uq_inventory_flight_class unique (flight_id, seat_class)
);

-- Inventory Reservations table
create table if not exists inventory_reservations (
  reservation_id uuid primary key,
  booking_id uuid not null,
  flight_id varchar(50) not null,
  seat_class varchar(20) not null,
  status varchar(30) not null,
  reason varchar(255),
  created_at timestamptz not null,
  constraint uq_reservation_booking unique (booking_id)
);

-- Booking Details table (snapshot of booking data for compensation)
create table if not exists booking_details (
  booking_id uuid primary key,
  flight_id varchar(50) not null,
  seat_class varchar(20) not null
);
