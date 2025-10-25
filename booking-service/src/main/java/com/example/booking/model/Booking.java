
package com.example.booking.model;
import jakarta.persistence.*;
import java.time.*;
@Entity
public class Booking {
    @Id @GeneratedValue private Long id;
    private Long userId; private Long roomId;
    private LocalDate startDate; private LocalDate endDate;
    private String status; private String requestId;
    private LocalDateTime createdAt = LocalDateTime.now();
    public Long getId(){return id;} public void setId(Long i){this.id=i;}
    public Long getUserId(){return userId;} public void setUserId(Long u){this.userId=u;}
    public Long getRoomId(){return roomId;} public void setRoomId(Long r){this.roomId=r;}
    public LocalDate getStartDate(){return startDate;} public void setStartDate(LocalDate d){this.startDate=d;}
    public LocalDate getEndDate(){return endDate;} public void setEndDate(LocalDate d){this.endDate=d;}
    public String getStatus(){return status;} public void setStatus(String s){this.status=s;}
    public String getRequestId(){return requestId;} public void setRequestId(String r){this.requestId=r;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime t){this.createdAt=t;}
}
