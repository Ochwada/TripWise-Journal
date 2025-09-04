package com.tripwise.TripJournal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ================================================================
 * Package Name: com.tripwise.TripJournal.config
 * Author      : Ochwada-GMK
 * Project Name: tripjournal
 * Date        : Thursday,  04.Sept.2025 | 15:53
 * Description :
 * ================================================================
 */
@Configuration
public class WebRedirectConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 1) Hitting "/" -> redirect to "/journal/"
        registry.addRedirectViewController("/", "/journal/");
        registry.addRedirectViewController("", "/journal/");      // extra safety
        registry.addRedirectViewController("/journal", "/journal/"); // canonical slash

        // 2) When at "/journal/", forward to the static file
        registry.addViewController("/journal/").setViewName("forward:/journal/index.html");
    }
}
