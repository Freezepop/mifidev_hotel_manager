
package com.example.hotel.model;
import jakarta.persistence.*;
@Entity
public class Hotel {
    @Id @GeneratedValue private Long id;
    private String name;
    private String address;
    public Long getId(){return id;} public void setId(Long i){this.id=i;}
    public String getName(){return name;} public void setName(String n){this.name=n;}
    public String getAddress(){return address;} public void setAddress(String a){this.address=a;}
}
