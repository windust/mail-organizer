package com.spinningnoodle.mail.organizer.service.email;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;


@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "imap")
@Data
public class ImapConfiguration {
    private String username;
    private String password;
    private String host;
    private int port;
    private boolean ssl;

}
