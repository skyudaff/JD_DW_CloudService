package org.example.cloudservice.dto;

import lombok.Builder;

@Builder
public record UserDto(String login, String password) {
}
