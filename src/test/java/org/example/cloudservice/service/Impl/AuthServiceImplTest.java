package org.example.cloudservice.service.Impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.cloudservice.dto.UserDto;
import org.example.cloudservice.security.JwtTokenProvider;
import org.example.cloudservice.dto.TokenDto;
import org.example.cloudservice.entity.UserEntity;
import org.example.cloudservice.exception.ErrorInputDataException;
import org.example.cloudservice.exception.ErrorUserException;
import org.example.cloudservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock
    MessageSource messageSource;
    @Mock
    UserRepository userRepository;
    @Mock
    JwtTokenProvider jwtTokenProvider;
    @Mock
    PasswordEncoder passwordEncoder;
    @InjectMocks
    AuthServiceImpl authService;

    UserEntity userEntity;
    UserDto userDTO;


    @BeforeEach
    void createTestUserEntity() {
        userDTO = UserDto.builder()
                .login("admin@example.org")
                .password("admin")
                .build();

        userEntity = UserEntity.builder()
                .id(1L)
                .login("admin@example.org")
                .password("admin")
                .roles(Collections.singleton(UserEntity.Roles.ROLE_USER))
                .build();
    }


    @Test
    void login_ValidUser_ReturnsTokenDTO() {
        // Arrange
        when(userRepository.findUserEntityByLogin(userDTO.login())).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(userDTO.password(), userEntity.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateAuthToken(userEntity)).thenReturn("token");

        // Act
        TokenDto result = authService.login(userDTO);

        // Assert
        assertNotNull(result);
        assertEquals("token", result.authToken());
    }

    @Test
    void login_InvalidPassword_ThrowsErrorInputDataException() {
        // Arrange
        when(userRepository.findUserEntityByLogin(userDTO.login())).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(userDTO.password(), userEntity.getPassword())).thenReturn(false);

        String expectedMessage = "Expected error message";
        when(messageSource.getMessage("user.password.error", null, LocaleContextHolder.getLocale()))
                .thenReturn(expectedMessage);

        // Act & Assert
        ErrorInputDataException exception = assertThrows(ErrorInputDataException.class, () -> authService.login(userDTO));

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void login_UserNotFound_ThrowsErrorUserException() {
        // Arrange
        when(userRepository.findUserEntityByLogin(userDTO.login())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ErrorUserException.class, () -> authService.login(userDTO));
    }

    @Test
    void logout_ValidUser_ReturnsTrue() {
        // Arrange
        Authentication auth = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(auth.getName()).thenReturn(userDTO.login());
        when(userRepository.findUserEntityByLogin(eq(userDTO.login()))).thenReturn(Optional.of(userEntity));

        // Act
        boolean result = authService.logout("token", mock(HttpServletRequest.class), mock(HttpServletResponse.class));

        // Assert
        assertTrue(result);
    }

    @Test
    void logout_InvalidUser_ReturnsFalse() {
        // Act
        boolean result = authService.logout("token", mock(HttpServletRequest.class), mock(HttpServletResponse.class));

        // Assert
        assertFalse(result);

        // Verify
        verify(userRepository, never()).findUserEntityByLogin(any());
        verify(jwtTokenProvider, never()).blacklistToken(any());
    }
}