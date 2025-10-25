
package com.example.hotel.controller;
import org.springframework.web.bind.annotation.*;
import com.example.hotel.repository.HotelRepository;
import com.example.hotel.repository.RoomRepository;
import com.example.hotel.model.Hotel;
import com.example.hotel.model.Room;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;
@RestController
@RequestMapping("/api")
public class HotelController {
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    public HotelController(HotelRepository hr, RoomRepository rr){ this.hotelRepository = hr; this.roomRepository = rr; }
    @PostMapping("/hotels")
    public Hotel addHotel(@RequestBody Hotel h){ return hotelRepository.save(h); }
    @PostMapping("/rooms")
    public Room addRoom(@RequestBody Room r){ return roomRepository.save(r); }
    @GetMapping("/hotels")
    public List<Hotel> listHotels(){ return hotelRepository.findAll(); }
    @GetMapping("/rooms")
    public List<Room> listAvailable(){ return roomRepository.findByAvailableTrue(); }
    @GetMapping("/rooms/recommend")
    public List<Room> recommend(){
        List<Room> available = roomRepository.findByAvailableTrue();
        available.sort(Comparator.comparingInt(Room::getTimesBooked).thenComparing(Room::getId));
        return available;
    }
    @PostMapping("/rooms/{id}/confirm-availability")
    @Transactional
    public ResponseEntity<String> confirmAvailability(@PathVariable Long id,
                                                      @RequestParam String requestId,
                                                      @RequestParam String start,
                                                      @RequestParam String end){
        Optional<Room> ro = roomRepository.findById(id);
        if(ro.isEmpty()) return ResponseEntity.notFound().build();
        Room r = ro.get();
        if(requestId.equals(r.getLockRequestId())) return ResponseEntity.ok("ALREADY_LOCKED");
        if(r.getLockRequestId()!=null){
            LocalDate s = r.getLockStart(); LocalDate e = r.getLockEnd();
            LocalDate ns = LocalDate.parse(start); LocalDate ne = LocalDate.parse(end);
            if(!(ne.isBefore(s) || ns.isAfter(e))){ return ResponseEntity.status(409).body("ROOM_LOCKED"); }
        }
        r.setLockRequestId(requestId);
        r.setLockStart(LocalDate.parse(start));
        r.setLockEnd(LocalDate.parse(end));
        roomRepository.save(r);
        return ResponseEntity.ok("LOCKED");
    }
    @PostMapping("/rooms/{id}/release")
    @Transactional
    public ResponseEntity<String> releaseLock(@PathVariable Long id, @RequestParam String requestId){
        Optional<Room> ro = roomRepository.findById(id);
        if(ro.isEmpty()) return ResponseEntity.notFound().build();
        Room r = ro.get();
        if(requestId.equals(r.getLockRequestId())){
            r.setLockRequestId(null); r.setLockStart(null); r.setLockEnd(null); roomRepository.save(r);
            return ResponseEntity.ok("RELEASED");
        } else { return ResponseEntity.ok("NO_OP"); }
    }
    @PostMapping("/rooms/{id}/finalize")
    @Transactional
    public ResponseEntity<String> finalizeBooking(@PathVariable Long id, @RequestParam String requestId){
        Optional<Room> ro = roomRepository.findById(id);
        if(ro.isEmpty()) return ResponseEntity.notFound().build();
        Room r = ro.get();
        if(requestId.equals(r.getLockRequestId())){
            r.setTimesBooked(r.getTimesBooked()+1); r.setLockRequestId(null); r.setLockStart(null); r.setLockEnd(null); roomRepository.save(r);
            return ResponseEntity.ok("FINALIZED");
        }
        r.setTimesBooked(r.getTimesBooked()+1); roomRepository.save(r);
        return ResponseEntity.ok("FINALIZED_NO_LOCK");
    }
}
