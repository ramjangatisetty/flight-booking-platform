-- Add loyalty-related fields to bookings table
alter table bookings add column member_id uuid;
alter table bookings add column loyalty_accrual_status varchar(20) default 'NONE';
alter table bookings add column loyalty_points integer;
alter table bookings add column loyalty_accrued_at timestamptz;

-- Index for fast lookup by member_id
create index idx_booking_member on bookings(member_id);

-- Index for finding failed accruals that need retry
create index idx_booking_loyalty_status on bookings(loyalty_accrual_status) where loyalty_accrual_status = 'FAILED';
