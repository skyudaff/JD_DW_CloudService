package org.example.cloudservice.util;

import io.jsonwebtoken.Claims;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.example.cloudservice.entity.UserEntity.Roles;
import org.example.cloudservice.security.JwtTokenAuth;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JwtTokenUtil {
    public static JwtTokenAuth generate(Claims claims) {
        JwtTokenAuth jwtInfoToken = new JwtTokenAuth();
        jwtInfoToken.setRoles(getRoles(claims));
        jwtInfoToken.setUsername(claims.getSubject());
        return jwtInfoToken;
    }

    private static Set<Roles> getRoles(Claims claims) {
        List<String> roles = claims.get("roles", List.class);
        return roles.stream()
                .map(Roles::valueOf)
                .collect(Collectors.toSet());
    }
}
