package com.myprojects.lovable_clone.security;

import com.myprojects.lovable_clone.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class AuthUtils {
    private static final long ACCESS_TOKEN_VALIDITY_MILLIS = 10 * 60 * 1000L;

    @Value("${app.jwt.secret-key}")
    private String jwtSecret;

    public SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + ACCESS_TOKEN_VALIDITY_MILLIS);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getId().toString())
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(getSecretKey())
                .compact();
    }

    public JwtUserPrincipal verifyAccessToken(String token){
        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Long userId = Long.valueOf(claims.get("userId", String.class));
        String username = claims.getSubject();

        return new JwtUserPrincipal(userId, username);

    }

    public Long getCurrentUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !(authentication.getPrincipal() instanceof JwtUserPrincipal)){
            throw new AuthenticationCredentialsNotFoundException("No Jwt Found");
        }
        JwtUserPrincipal user = (JwtUserPrincipal) authentication.getPrincipal();
        return user.getUserId();
    }


}
