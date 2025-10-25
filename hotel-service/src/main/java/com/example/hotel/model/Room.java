
package com.example.hotel.model;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity
public class Room {
    @Id @GeneratedValue private Long id;
    private Long hotelId;
    private String number;
    private boolean available = true;
    private int timesBooked = 0;
    private String lockRequestId;
    private LocalDate lockStart;
    private LocalDate lockEnd;
    public Long getId(){return id;} public void setId(Long i){this.id=i;}
    public Long getHotelId(){return hotelId;} public void setHotelId(Long h){this.hotelId=h;}
    public String getNumber(){return number;} public void setNumber(String n){this.number=n;}
    public boolean isAvailable(){return available;} public void setAvailable(boolean a){this.available=a;}
    public int getTimesBooked(){return timesBooked;} public void setTimesBooked(int t){this.timesBooked=t;}
    public String getLockRequestId(){return lockRequestId;} public void setLockRequestId(String r){this.lockRequestId=r;}
    public LocalDate getLockStart(){return lockStart;} public void setLockStart(LocalDate d){this.lockStart=d;}
    public LocalDate getLockEnd(){return lockEnd;} public void setLockEnd(LocalDate d){this.lockEnd=d;}
}
