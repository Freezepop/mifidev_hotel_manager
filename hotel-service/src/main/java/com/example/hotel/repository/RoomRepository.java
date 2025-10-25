
package com.example.hotel.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.hotel.model.Room;
import java.util.List;
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByAvailableTrue();
}
