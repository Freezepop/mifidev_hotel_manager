
package com.example.booking.security;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
public class JwtUtil {
    private final Key key = Keys.hmacShaKeyFor("supersecretjwtkeysupersecretjwtkey".getBytes());
    public String generateToken(String username, String role) {
        return Jwts.builder().setSubject(username).claim("role", role)
            .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis()+3600_000))
            .signWith(key).compact();
    }
    public Jws<Claims> parseToken(String token) { return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token); }
}
