package com.spinningnoodle.mail.organizer.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@ConfigurationProperties(prefix = "classifier-configuration")
@Data
public class ClassifierConfiguration {
    private boolean useBody;
    Map<String, String> descriptions;

    public String forPrompt() {
        return descriptions.entrySet().stream()
                .map(e -> String.format("%s: %s", e.getKey(), e.getValue()))
                .collect(Collectors.joining("\n\n"));
    }

    public String identify(String inputString) {
        for (Map.Entry<String, String> entry : descriptions.entrySet()) {
            if (inputString.toLowerCase().contains(entry.getKey().toLowerCase())) {
                return entry.getKey();
            }
        }
        return "Other";
    }

    public Set<String> getFolders() {
        return getDescriptions().keySet();
    }
}
