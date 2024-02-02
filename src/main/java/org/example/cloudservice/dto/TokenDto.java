package org.example.cloudservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenDto(@JsonProperty("auth-token") String authToken) {
}
