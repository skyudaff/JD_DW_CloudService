package org.example.cloudservice.service.Impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudservice.dto.TokenDto;
import org.example.cloudservice.dto.UserDto;
import org.example.cloudservice.entity.UserEntity;
import org.example.cloudservice.exception.ErrorInputDataException;
import org.example.cloudservice.exception.ErrorUserException;
import org.example.cloudservice.repository.UserRepository;
import org.example.cloudservice.security.JwtTokenProvider;
import org.example.cloudservice.service.AuthService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final MessageSource messageSource;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    public TokenDto login(@NonNull UserDto userDto) {
        final var message = messageSource.getMessage("user.password.error", null, LocaleContextHolder.getLocale());
        var user = findUserByLogin(userDto.login());
        if (isEquals(userDto, user)) {
            String token = jwtTokenProvider.generateAuthToken(user);
            return new TokenDto(token);
        } else {
            throw new ErrorInputDataException(message, user.getId());
        }
    }

    @Override
    public boolean logout(String authToken, HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            var user = findUserByLogin(auth.getName());
            SecurityContextLogoutHandler securityContextLogoutHandler =
                    new SecurityContextLogoutHandler();
            if (user != null) {
                securityContextLogoutHandler.logout(request, response, auth);
                jwtTokenProvider.blacklistToken(authToken);
                return true;
            }
        }
        return false;
    }

    private UserEntity findUserByLogin(String login) {
        final var message = messageSource.getMessage("user.login.error", null, LocaleContextHolder.getLocale());
        return userRepository.findUserEntityByLogin(login).orElseThrow(() ->
                new ErrorUserException(message, 0));
    }

    private boolean isEquals(UserDto userDto, UserEntity userFromDatabase) {
        return passwordEncoder.matches(userDto.password(), userFromDatabase.getPassword());
    }
}
