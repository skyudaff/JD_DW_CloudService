package org.example.cloudservice.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudservice.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends GenericFilterBean {
    private static final String AUTHORIZATION = "Authorization";
    @Value("${auth.header}")
    private String AUTH_TOKEN_HEADER;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = getTokenFromRequest((HttpServletRequest) request);

        if (token != null && jwtTokenProvider.validateAuthToken(token)) {
            Claims claims = jwtTokenProvider.getAccessClaims(token);
            JwtTokenAuth jwtInfoToken = JwtTokenUtil.generate(claims);
            jwtInfoToken.setAuthenticated(true);
            SecurityContextHolder.getContext().setAuthentication(jwtInfoToken);
        }
        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearer = request.getHeader(AUTHORIZATION);
        String bearerToken = request.getHeader(AUTH_TOKEN_HEADER);
        return bearer == null ? getTokenFromHeader(bearerToken) : getTokenFromHeader(bearer);
    }

    private String getTokenFromHeader(String bearer) {
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
