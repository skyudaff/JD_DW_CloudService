package org.example.cloudservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record FileDto(@JsonProperty("filename") String fileName, String hash,
                      Long size, byte[] fileBytes, String type, LocalDateTime date) {
}
