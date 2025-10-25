
package com.example.booking.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.booking.model.Booking;
import java.util.Optional;
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByRequestId(String requestId);
}
