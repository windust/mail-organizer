package com.spinningnoodle.mail.organizer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.Duration;

@SpringBootApplication
public class OrganizerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrganizerApplication.class, args);
    }


    @Bean
    public RestClientCustomizer restClientCustomizer() {
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setConnectTimeout(Duration.ofSeconds(120L));
        simpleClientHttpRequestFactory.setReadTimeout(Duration.ofSeconds(120L));

        return restClientBuilder -> {
            restClientBuilder.requestFactory(simpleClientHttpRequestFactory)
                    .build();
        };
    }
}
