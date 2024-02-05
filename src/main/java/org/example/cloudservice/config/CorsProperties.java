package org.example.cloudservice.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "allowed")
public class CorsProperties {
    private final String origins;
    private final boolean allowCredentials;
    private final String allowedMethods;
    private final String allowedHeaders;
}