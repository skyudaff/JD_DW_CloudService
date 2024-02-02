package org.example.cloudservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.cloudservice.dto.TokenDto;
import org.example.cloudservice.dto.UserDto;
import org.example.cloudservice.service.Impl.AuthServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthServiceImpl authService;

    @PostMapping("/login")
    public TokenDto login(@RequestBody UserDto user) {
        return authService.login(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("auth-token") String authToken,
                                    HttpServletRequest request, HttpServletResponse response) {
        return (authService.logout(authToken, request, response))
                ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }
}
