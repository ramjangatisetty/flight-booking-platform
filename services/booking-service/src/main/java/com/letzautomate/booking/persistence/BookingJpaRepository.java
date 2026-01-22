package com.letzautomate.booking.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingJpaRepository extends JpaRepository<BookingEntity, UUID> {}
