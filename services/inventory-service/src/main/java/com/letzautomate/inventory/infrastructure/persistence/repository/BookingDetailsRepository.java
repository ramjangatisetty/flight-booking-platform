package com.letzautomate.inventory.infrastructure.persistence.repository;

import com.letzautomate.inventory.infrastructure.persistence.entity.BookingDetailsEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingDetailsRepository extends JpaRepository<BookingDetailsEntity, UUID> {
}