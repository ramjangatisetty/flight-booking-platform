-- Add bag_tag column to bookings table for baggage integration
ALTER TABLE bookings ADD COLUMN bag_tag VARCHAR(10);

-- Add index for efficient bagTag lookups
CREATE INDEX idx_bookings_bag_tag ON bookings(bag_tag);

-- Add comment
COMMENT ON COLUMN bookings.bag_tag IS 'Baggage tag assigned when booking is confirmed (format: AA12345678)';
