package com.tripwise.TripJournal.dto.responses;

import com.tripwise.TripJournal.dto.MediaSummary;
import com.tripwise.TripJournal.dto.MetadataDTO;
import lombok.*;

import java.time.Instant;
import java.util.List;

/**
 * ================================================================
 * Package Name: com.tripwise.TripJournal.dto.responses
 * Author      : Ochwada-GMK
 * Project Name: tripjournal
 * Date        : Thursday,  28.Aug.2025 | 13:41
 * Description :
 * ================================================================
 */
@Data
@Builder
public class JournalResponse {
    /**
     * Unique identifier for the journal entry.
     */
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
     * The title of the activity recorded in this journal entry.
     * Example: {@code "Hiking in Aberdare Forest"}
     */
    private String title;

    /**
     * A detailed description of the activity.
     * Example: {@code "A full-day hike exploring waterfalls and wildlife."}
     */
    private String description;


    /**
     * The city where the activity took place.
     */
    private String city;

    /**
     * The country where the activity took place.
     */
    private String country;

    //private String coverMediaId;

    private List<String> mediaIds;

    /**
     * A list of tags associated with this journal entry for easier categorization or search.
     * Example: {@code ["hiking", "nature", "adventure"]}
     */
    private List<String> tags;

    /**
     * Arbitrary key-value pairs that provide additional context or details about this journal entry.
     * *
     * -This field is flexible and can store extra information such as weather, mood, GPS coordinates, or any custom
     * metadata provided by the user or system.
     */
    private MetadataDTO metadata;

    /**
     * The timestamp when this journal entry was created.
     */
    private Instant createdDate;

    /**
     * The timestamp when this journal entry was last modified.
     */
    private Instant modifiedDate;

    //private List<MediaSummary> media;
    //private MediaSummary coverMedia;


}
