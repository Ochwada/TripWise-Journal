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

    @Schema(description = "Itinerary identifier this journal belongs to", example = "12")
    private String itineraryId;

    @Schema(example = "Berlin")
    private String city;

    @Schema(example = "Germany")
    private String country;

    private String coverMediaId;

    @Schema(example = "[\"hiking\",\"nature\",\"adventure\"]")
    private List<@Size(max = 40, message = "Tag must be at most 40 characters") String> tags;


    @Schema(description = "Media IDs from media-service attached to this journal",
            example = "[\"media-001\",\"media-002\"]")
    private List<String> mediaIds;

    public Map<String, Object> getMetadata() {
        return Map.of();
    }

    // Optional: include a version for optimistic locking if you store one in Journal.
    // private Long version;
}
