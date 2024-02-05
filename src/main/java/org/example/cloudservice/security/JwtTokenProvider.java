package org.example.cloudservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudservice.config.JwtTokenConfig;
import org.example.cloudservice.entity.UserEntity;
import org.example.cloudservice.entity.UserRoles;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final JwtTokenConfig jwtTokenConfig;
    private UserEntity currentLoggedUser;
    private final Set<String> blacklistedTokens = new HashSet<>();

    public UserEntity getAuthorizedUser() {
        return currentLoggedUser;
    }

    public String generateAuthToken(@NonNull UserEntity user) {
        currentLoggedUser = user;
        LocalDateTime now = LocalDateTime.now();
        Instant accessExpirationInstant = now.plusMinutes(jwtTokenConfig.getExpiration())
                .atZone(ZoneId.systemDefault()).toInstant();
        Date accessExpiration = Date.from(accessExpirationInstant);

        Set<UserRoles> roles = user.getRoles();

        return Jwts.builder()
                .setId(String.valueOf(user.getId()))
                .setSubject(user.getLogin())
                .setExpiration(accessExpiration)
                .signWith(jwtTokenConfig.getSecret())
                .claim("roles", roles)
                .compact();
    }

    public boolean validateAuthToken(@NonNull String accessToken) {
        if (blacklistedTokens.contains(accessToken)) {
            return false;
        }

        try {
            Jwts.parserBuilder()
                    .setSigningKey(jwtTokenConfig.getSecret())
                    .build()
                    .parseClaimsJws(accessToken);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid signature for token: {}", accessToken, e);
        } catch (MalformedJwtException e) {
            log.error("Malformed jwt for token: {}", accessToken, e);
        } catch (ExpiredJwtException e) {
            log.error("Token expired for token: {}", accessToken, e);
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported jwt for token: {}", accessToken, e);
        } catch (IllegalArgumentException e) {
            log.error("Invalid token: {}", accessToken, e);
        }
        return false;
    }

    public Claims getAccessClaims(@NonNull String token) {
        return getClaimsFromToken(token, jwtTokenConfig.getSecret());
    }

    private Claims getClaimsFromToken(@NonNull String token, @NonNull SecretKey secret) {
        return Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public void blacklistToken(String authToken) {
        blacklistedTokens.add(authToken);
    }
}
