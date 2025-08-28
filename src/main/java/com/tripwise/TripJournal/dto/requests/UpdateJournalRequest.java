package com.tripwise.TripJournal.dto.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.*;

/**
 * ================================================================
 * Package Name: com.tripwise.TripJournal.dto
 * Author      : Ochwada-GMK
 * Project Name: tripjournal
 * Date        : Thursday,  28.Aug.2025 | 13:26
 * Description :
 * ================================================================
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateJournalRequest {

    @Schema(example = "Hiking in Aberdare Forest")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Schema(example = "A full-day hike exploring waterfalls and wildlife.")
    private String description;

    @Schema(description = "Itinerary identifier this journal belongs to", example = "TRIP-789")
    private String itineraryId;

    @Schema(example = "Kisumu")
    private String city;

    @Schema(example = "Kenya")
    private String country;

    @Schema(example = "[\"hiking\",\"nature\",\"adventure\"]")
    private List<@Size(max = 40, message = "Tag must be at most 40 characters") String> tags;


    @Schema(description = "Media IDs from media-service attached to this journal",
            example = "[\"media-001\",\"media-002\"]")
    private List<String> mediaIds;

    // Optional: include a version for optimistic locking if you store one in Journal.
    // private Long version;
}
