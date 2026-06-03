package com.hacker.boooks.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
@SuppressWarnings("unused")
public class CustomInfoContributor implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("application", "Boooks");
        builder.withDetail("version", "1.0");
        builder.withDetail("description", "Library Management System");
        builder.withDetail("environment", getEnvironmentInfo());
        builder.withDetail("dependencies", getDependencyInfo());
        builder.withDetail("contact", "https://github.com/thehackermonk");
        builder.withDetail("documentation", "http://localhost:8080/swagger-ui/index.html#/");
    }

    private Map<String, Object> getEnvironmentInfo() {
        Map<String, Object> environmentInfo = new HashMap<>();
        environmentInfo.put("server", "Tomcat");
        environmentInfo.put("java", System.getProperty("java.version"));
        return environmentInfo;
    }

    private Map<String, Object> getDependencyInfo() {
        Map<String, Object> dependencyInfo = new HashMap<>();
        dependencyInfo.put("spring-boot", "3.1.0");
        return dependencyInfo;
    }

}
