package com.travelbuddy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configura o cliente HTTP usado para chamar as APIs externas
 * (Geoapify Geocoding, OpenWeather, Booking.com e Google Flights via RapidAPI).
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .codecs(config -> config.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)) // 2MB buffer
                .build();
    }
}
