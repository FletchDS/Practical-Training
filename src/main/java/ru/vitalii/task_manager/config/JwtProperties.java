package ru.vitalii.task_manager.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "application.security.jwt")
public class JwtProperties {

    private String secretKey;
    private long expiration;
    private RefreshToken refreshToken = new RefreshToken();

    @Getter
    @Setter
    public static class RefreshToken {
        private long expiration;
    }
}