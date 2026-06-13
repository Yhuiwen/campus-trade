package com.campus.trade.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generate(LoginUser user) {
        return Jwts.builder().subject(user.username())
                .claim("id", user.id()).claim("role", user.role())
                .issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key()).compact();
    }

    public LoginUser parse(String token) {
        Claims claims = Jwts.parser().verifyWith(key()).build()
                .parseSignedClaims(token).getPayload();
        return new LoginUser(claims.get("id", Long.class), claims.getSubject(), claims.get("role", String.class));
    }
}
