-- Bags table
CREATE TABLE bags (
    bag_tag VARCHAR(10) PRIMARY KEY,
    booking_id UUID NOT NULL,
    passenger_id UUID NOT NULL,
    origin VARCHAR(3) NOT NULL,
    destination VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Bag events table
CREATE TABLE bag_events (
    id BIGSERIAL PRIMARY KEY,
    bag_tag VARCHAR(10) NOT NULL REFERENCES bags(bag_tag),
    event_type VARCHAR(20) NOT NULL,
    airport VARCHAR(3),
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Index for efficient event lookups by bag_tag
CREATE INDEX idx_bag_events_bag_tag ON bag_events(bag_tag);
