package com.tripwise.TripJournal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * ================================================================
 * Package Name: com.tripwise.TripJournal.config
 * Author      : Ochwada-GMK
 * Project Name: tripjournal
 * Date        : Monday,  01.Sept.2025 | 11:41
 * Description :Configuration class that enables auditing support for MongoDB
 * ================================================================
 */
@Configuration
@EnableMongoAuditing
public class MongoConfig {
    /**
     * {@code @EnableMongoAuditing} annotation enables Spring Data to automatically populate fields annotated with {@code
     * @CreatedDate} and {@code @LastModifiedDate} during entity persistence.
     * *
     * This class does not require any methods or fields; its presence in the application context is sufficient for
     * activating auditing behavior.
     * */
}
