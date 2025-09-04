package com.tripwise.TripJournal.service;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * ================================================================
 * Package Name: com.tripwise.TripJournal.service
 * Author      : Ochwada-GMK
 * Project Name: tripjournal
 * Date        : Thursday,  04.Sept.2025 | 12:40
 * Description :
 * ================================================================
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceHelpers {
    private final JournalEnricher enricher;

    @Value("${journal.enrichment.enabled:true}")
    private boolean enrichmentEnabled;


    public void safeCall(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            log.warn("Downstream media call failed (continuing): {}", e.getMessage());
            log.debug("Stacktrace:", e);
        }
    }

    public Map<String, Object> tryAutoMetadata(String city, String country) {
        if (!enrichmentEnabled || (city == null && country == null)) return Collections.emptyMap();
        try {
            return enricher.buildAutoMetadata(city, country);
        } catch (Exception e) {
            log.warn("Enrichment failed, continuing without it: {}", e.getMessage());
            log.debug("Enrichment stack:", e);
            return Collections.emptyMap();
        }
    }

}
