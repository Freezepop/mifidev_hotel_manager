
package com.example.booking.controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import com.example.booking.model.*;
import com.example.booking.repository.*;
import com.example.booking.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDate;
import java.util.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
@RestController
@RequestMapping("/api")
public class BookingController {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil = new JwtUtil();
    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${hotel.service.url:http://localhost:8090/api}")
    private String hotelServiceUrl;
    public BookingController(BookingRepository br, UserRepository ur, PasswordEncoder pe){
        this.bookingRepository = br; this.userRepository = ur; this.passwordEncoder = pe;
    }
    @PostMapping("/user/register")
    public ResponseEntity<?> register(@RequestBody User u){
        if(userRepository.findByUsername(u.getUsername()).isPresent()) return ResponseEntity.status(HttpStatus.CONFLICT).body("User exists");
        u.setRole(u.getRole()==null? "USER": u.getRole());
        u.setPassword(passwordEncoder.encode(u.getPassword()));
        userRepository.save(u);
        String token = jwtUtil.generateToken(u.getUsername(), u.getRole());
        return ResponseEntity.ok(Map.of("token", token));
    }
    @PostMapping("/user/auth")
    public ResponseEntity<?> auth(@RequestBody Map<String,String> req){
        String username = req.get("username"); String password = req.get("password");
        var user = userRepository.findByUsername(username);
        if(user.isEmpty() || !passwordEncoder.matches(password, user.get().getPassword())) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        String token = jwtUtil.generateToken(username, user.get().getRole());
        return ResponseEntity.ok(Map.of("token", token));
    }
    @PostMapping("/booking")
    @Transactional
    public ResponseEntity<?> createBooking(@RequestBody Map<String, Object> req){
        String requestId = (String)req.get("requestId"); if(requestId==null) return ResponseEntity.badRequest().body("requestId required");
        Optional<Booking> existing = bookingRepository.findByRequestId(requestId);
        if(existing.isPresent()) return ResponseEntity.ok(existing.get());
        Booking b = new Booking();
        b.setUserId(((Number)req.get("userId")).longValue());
        b.setRoomId(((Number)req.get("roomId")).longValue());
        b.setStartDate(LocalDate.parse((String)req.get("startDate")));
        b.setEndDate(LocalDate.parse((String)req.get("endDate")));
        b.setStatus("PENDING"); b.setRequestId(requestId);
        bookingRepository.save(b);
        String confirmUrl = hotelServiceUrl + "/rooms/" + b.getRoomId() + "/confirm-availability?requestId=" + requestId
            + "&start=" + b.getStartDate() + "&end=" + b.getEndDate();
        int maxAttempts = 3; int attempt = 0; boolean confirmed = false;
        while(attempt < maxAttempts){
            attempt++;
            try{ ResponseEntity<String> resp = restTemplate.postForEntity(confirmUrl, null, String.class);
                if(resp.getStatusCode().is2xxSuccessful()){ confirmed = true; break; }
                else if(resp.getStatusCode().value() == 409){ break; }
            } catch(Exception ex){ try{ Thread.sleep(500L * attempt); } catch(InterruptedException e){} }
        }
        if(confirmed){
            try{ String finalizeUrl = hotelServiceUrl + "/rooms/" + b.getRoomId() + "/finalize?requestId=" + requestId; restTemplate.postForEntity(finalizeUrl, null, String.class); } catch(Exception ex){}
            b.setStatus("CONFIRMED"); bookingRepository.save(b); return ResponseEntity.ok(b);
        } else {
            b.setStatus("CANCELLED"); bookingRepository.save(b);
            try{ String releaseUrl = hotelServiceUrl + "/rooms/" + b.getRoomId() + "/release?requestId=" + requestId; restTemplate.postForEntity(releaseUrl, null, String.class); } catch(Exception ex){}
            return ResponseEntity.status(HttpStatus.CONFLICT).body(b);
        }
    }
    @GetMapping("/bookings")
    public List<Booking> listBookings(@RequestParam Long userId){ return bookingRepository.findAll().stream().filter(b->b.getUserId().equals(userId)).toList(); }
    @GetMapping("/booking/{id}")
    public ResponseEntity<Booking> getBooking(@PathVariable Long id){ return bookingRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()); }
    @DeleteMapping("/booking/{id}")
    @Transactional
    public ResponseEntity<?> cancelBooking(@PathVariable Long id){
        Optional<Booking> ob = bookingRepository.findById(id); if(ob.isEmpty()) return ResponseEntity.notFound().build();
        Booking b = ob.get();
        if("CANCELLED".equals(b.getStatus())) return ResponseEntity.ok("ALREADY_CANCELLED");
        if("CONFIRMED".equals(b.getStatus())){
            try{ String releaseUrl = hotelServiceUrl + "/rooms/" + b.getRoomId() + "/release?requestId=" + b.getRequestId(); restTemplate.postForEntity(releaseUrl, null, String.class); } catch(Exception ex){}
        }
        b.setStatus("CANCELLED"); bookingRepository.save(b); return ResponseEntity.ok(b);
    }
}
