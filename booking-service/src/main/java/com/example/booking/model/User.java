
package com.example.booking.model;
import jakarta.persistence.*;
@Entity
@Table(name="users")
public class User {
    @Id @GeneratedValue private Long id;
    private String username;
    private String password;
    private String role;
    public Long getId(){return id;} public void setId(Long i){this.id=i;}
    public String getUsername(){return username;} public void setUsername(String s){this.username=s;}
    public String getPassword(){return password;} public void setPassword(String p){this.password=p;}
    public String getRole(){return role;} public void setRole(String r){this.role=r;}
}
