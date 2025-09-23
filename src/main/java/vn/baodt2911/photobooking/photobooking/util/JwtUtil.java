package vn.baodt2911.photobooking.photobooking.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.access.secret}")
    private String accessSecret;

    @Value("${jwt.refresh.secret}")
    private String refreshSecret;

    @Value("${jwt.access.expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpiration;

    private SecretKey  accessKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));
    }

    private SecretKey  refreshKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret));
    }

    // ===== TẠO TOKEN =====
    public String generateAccessToken(String email) {
        return Jwts.builder()
                .subject(email)
                .expiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(accessKey())
                .compact();
    }

    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .subject(email)
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(refreshKey())
                .compact();
    }

    // ===== GIẢI TOKEN =====
    private Claims parse(String token, boolean isRefresh) {
        return Jwts.parser()
                .verifyWith(isRefresh ? refreshKey() : accessKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token, boolean isRefresh) {
        return parse(token, isRefresh).getSubject();
    }

    public boolean validate(String token, boolean isRefresh) {
        try {
            parse(token, isRefresh);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
