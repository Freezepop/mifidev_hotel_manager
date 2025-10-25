
package com.example.booking.security;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.io.IOException;
import java.security.Key;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final Key key = Keys.hmacShaKeyFor("supersecretjwtkeysupersecretjwtkey".getBytes());
    @Bean public PasswordEncoder passwordEncoder(){ return new BCryptPasswordEncoder(); }
    @Bean public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf->csrf.disable())
            .authorizeHttpRequests(auth->auth
                .requestMatchers("/api/user/register","/api/user/auth").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(new OncePerRequestFilter(){
                @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                    throws ServletException, IOException {
                    String header = request.getHeader("Authorization");
                    if(header != null && header.startsWith("Bearer ")) {
                        String token = header.substring(7);
                        try { Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
                            String username = claims.getBody().getSubject();
                            String role = claims.getBody().get("role", String.class);
                            var auth = new UsernamePasswordAuthenticationToken(username, null, List.of(() -> "ROLE_"+role));
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        } catch(JwtException ex){ response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT"); return; }
                    }
                    chain.doFilter(request, response);
                }
            }, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
