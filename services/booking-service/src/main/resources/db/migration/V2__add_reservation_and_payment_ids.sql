-- Add reservation_id and payment_id columns to bookings table
alter table bookings add column if not exists reservation_id uuid;
alter table bookings add column if not exists payment_id uuid;

-- Add index on reservation_id for faster lookups
create index if not exists idx_bookings_reservation_id on bookings(reservation_id);
