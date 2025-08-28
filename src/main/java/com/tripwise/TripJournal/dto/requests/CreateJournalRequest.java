package com.tripwise.TripJournal.dto.requests;

import com.tripwise.TripJournal.model.Journal;
import jakarta.validation.constraints.*;
import lombok.*;


import java.util.*;

/**
 * ================================================================
 * Package Name: com.tripwise.TripJournal.dto
 * Author      : Ochwada-GMK
 * Project Name: tripjournal
 * Date        : Thursday,  28.Aug.2025 | 12:55
 * Description : Request payload for creating a new {@link Journal} entry.
 * ================================================================
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateJournalRequest {

    /**
     * The title of the activity recorded in the journal entry.
     * Example: {@code "Hiking in Aberdare Forest"}
     */
    @NotBlank(message = "Title must not be blank")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    /**
     * A detailed description of the activity.
     * Example: {@code "A full-day hike exploring waterfalls and wildlife."}
     */
    private String description;


    /**
     * Identifier of the itinerary/ trip this journal entry belongs to.
     */
    private String itineraryId;

    /**
     * City where the activity took place.
     */
    private String city;

    /**
     * The country where the activity took place.
     */
    private String country;

    /**
     * A list of tags associated with the journal entry.
     * Example: {@code ["hiking", "nature", "adventure"]}
     */
    private List<String> tags;


    /**
     * Arbitrary key-value pairs providing additional context, enriched by external APIs or user input.
     *   <li>weather – from OpenWeather API</li>
     *   <li>gps – from Geocoding API or device</li>
     *   <li>mood  – user-provided or inferred</li>
     * </ul>
     */
    private Map<String, Object> metadata;

    /**
     * List of media items (from media-service) attached to this journal entry.
     * These may include photos, videos, or audio recordings.
     * *
     * Example:
     * "media": [
     *   { "id": "media-001", "url": "https://cdn.example.com/photo1.jpg", "type": "image" }
     * ]
     */
    private List<String> mediaIds; // store IDs only, resolve via media-service
}
