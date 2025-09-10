package com.tripwise.TripJournal.config;
import org.springframework.context.annotation.*;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * ================================================================
 * Package Name: com.tripwise.TripJournal.config
 * Author      : Ochwada-GMK
 * Project Name: tripjournal
 * Date        : Wednesday,  10.Sept.2025 | 10:01
 * Description :
 * ================================================================
 */
@Configuration
public class WebClientConfig {
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(c -> c.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                        .build())
                .build();
    }
}
