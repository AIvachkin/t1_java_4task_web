package ru.t1.java.demo.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Profile("test")
public class TestCheckClientConfig {

    @Value("${integration.url}")
    private String url;

    @Bean
    public CheckWebClient testCheckWebClient(WebClient.Builder webClientBuilder) {
        return new CheckWebClient(webClientBuilder
                .baseUrl(url)
                .build());
    }
}