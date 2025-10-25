
package com.example.hotel.security;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.security.Key;
import java.util.List;
public class JwtFilter extends OncePerRequestFilter {
    private final Key key = Keys.hmacShaKeyFor("supersecretjwtkeysupersecretjwtkey".getBytes());
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if(header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try { Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
                String username = claims.getBody().getSubject();
                String role = claims.getBody().get("role", String.class);
                var auth = new UsernamePasswordAuthenticationToken(username, null, List.of(() -> "ROLE_" + role));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch(Exception ex) { response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT"); return; }
        }
        chain.doFilter(request, response);
    }
}
