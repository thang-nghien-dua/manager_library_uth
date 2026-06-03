package com.hacker.boooks.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @SuppressWarnings("unused")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Tắt hoàn toàn CORS (Lambda DSL - Spring Boot 3.x)
                .cors(AbstractHttpConfigurer::disable)
                // Tắt hoàn toàn CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // Mở khóa tự do tất cả các request (Swagger UI, /author, /members, /logs, ...)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }

}
