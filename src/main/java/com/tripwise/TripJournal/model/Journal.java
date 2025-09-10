package com.tripwise.TripJournal.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;
import java.time.Instant;

/**
 * ================================================================
 * Package Name: com.tripwise.TripJournal.model
 * Author      : Ochwada-GMK
 * Project Name: tripjournal
 * Date        : Thursday,  28.Aug.2025 | 12:00
 * Description : Represents a journal entry stored in MongoDB {@code journals} collection.
 * - Each entry is linked to user and an itinerary, and records travel-related activities, descriptions and associated
 * location details.
 * ================================================================
 */
@Document(collection = "journal")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Journal {
    /**
     * Unique identifier for the journal entry.
     */
    @Id
    private String id;

    /**
     * The identifier of the user who created this journal entry.
     */
    private String userId;

    /**
     * The identifier of the itinerary this journal entry belongs to.
     */
    private String itineraryId;

    /**
     * The city where the activity took place.
     */
    private String city;

    /**
     * The country where the activity took place.
     */
    private String country;

    /**
     * The title of the activity recorded in this journal entry.
     * Example: {@code "Hiking in Aberdare Forest"}
     */
    private String title;

    /**
     * A detailed description of the activity.
     * Example: {@code "A full-day hike exploring waterfalls and wildlife."}
     */
    private String description;

    // -------------- Media  ------------------------------------

    // The cover image/video (media ID from TripMedia)
    //private String coverMediaId;

    //private List<String>  mediaIds;
    @Builder.Default
    private List<String> mediaIds = new ArrayList<>();

    // --------------optional tags & metadata ------------------------------------

    /**
     * A list of tags associated with this journal entry for easier categorization or search.
     * Example: {@code ["hiking", "nature", "adventure"]}
     */
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    /**
     * Arbitrary key-value pairs that provide additional context or details about this journal entry.
     * *
     * -This field is flexible and can store extra information such as weather, mood, GPS coordinates, or any custom
     * metadata provided by the user or system.
     */
    private Map<String, Object> metadata;

    /**
     * The timestamp when this journal entry was created.
     */
    private Instant createdDate;

    /**
     * The timestamp when this journal entry was last modified.
     */
    private Instant modifiedDate;
}

