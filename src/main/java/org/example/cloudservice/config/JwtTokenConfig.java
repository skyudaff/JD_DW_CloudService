package org.example.cloudservice.config;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Getter
@Configuration
public class JwtTokenConfig {
    private final SecretKey secret;
    @Value("${jwt.expiration}")
    private int expiration;

    public JwtTokenConfig(@Value("${jwt.secret}") String secret) {
        this.secret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }
}