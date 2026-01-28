package com.letzautomate.booking.infrastructure.persistence.repository;

import com.letzautomate.booking.infrastructure.persistence.entity.BookingEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingJpaRepository extends JpaRepository<BookingEntity, UUID> {
	
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("UPDATE BookingEntity b SET b.bagTag = :bagTag, b.updatedAt = CURRENT_TIMESTAMP WHERE b.bookingId = :bookingId")
	int updateBagTag(@Param("bookingId") UUID bookingId, @Param("bagTag") String bagTag);
}
