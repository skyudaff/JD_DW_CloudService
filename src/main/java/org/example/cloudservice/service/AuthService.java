package org.example.cloudservice.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.cloudservice.dto.TokenDto;
import org.example.cloudservice.dto.UserDto;

public interface AuthService {
    TokenDto login(UserDto userDTO);

    boolean logout(String token, HttpServletRequest request, HttpServletResponse response);
}
