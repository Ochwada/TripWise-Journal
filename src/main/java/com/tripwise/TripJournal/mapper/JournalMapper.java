package com.tripwise.TripJournal.mapper;

import com.tripwise.TripJournal.dto.requests.CreateJournalRequest;
import com.tripwise.TripJournal.dto.requests.UpdateJournalRequest;
import com.tripwise.TripJournal.dto.responses.JournalResponse;
import com.tripwise.TripJournal.model.Journal;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ================================================================
 * Package Name: com.tripwise.TripJournal.mapper
 * Author      : Ochwada-GMK
 * Project Name: tripjournal
 * Date        : Thursday,  28.Aug.2025 | 15:10
 * Description :
 * ================================================================
 */
@Component
public final class JournalMapper {
    private JournalMapper() {
    }

    /* ---------- CREATE ---------- */
    public static Journal toEntity(CreateJournalRequest request, String userId) {

        Instant now = Instant.now();

        return Journal.builder()
                .userId(userId)
                .itineraryId(trimToNull(request.getItineraryId()))
                .city(trimToNull(request.getCity()))
                .country(trimToNull(request.getCountry()))
                .title(trimToNull(request.getTitle()))
                .description(trimToNull(request.getDescription()))
                .tags(copyToList(request.getTags()))
                // metadata (gps + weather) is set by the service after persisting
                .createdDate(now)
                .modifiedDate(now)
                .build();
    }

    /* ---------- UPDATE (PATCH) ---------- */

    /**
     * Applies non-null fields from request to target.
     *
     * @return true if city or country changed (caller should refresh GPS + weather)
     */
    public static boolean applyUpdate(UpdateJournalRequest request, Journal targetJournal) {

        boolean locationChanged = false;

        if (request.getTitle() != null) {
            targetJournal.setTitle(trimToNull(request.getTitle()));
        }
        if (request.getDescription() != null) {
            targetJournal.setDescription(trimToNull(request.getDescription()));
        }

        if (request.getItineraryId() != null) {
            targetJournal.setItineraryId(request.getItineraryId());
        }

        if (request.getCity() != null) {
            String newCountry = trimToNull(request.getCity());

            if (!Objects.equals(newCountry, targetJournal.getCountry())) {
                targetJournal.setCountry(newCountry);

                locationChanged = true;
            }
        }

        if (request.getTags() != null) {
            targetJournal.setTags(copyToList(request.getTags()));
        }

        // metadata (gps + weather) remains service-managed; do not read from request.
        targetJournal.setModifiedDate(Instant.now());
        return locationChanged;
    }

    public JournalResponse toResponse(Journal journal) {
        return JournalResponse.builder()
                .id(journal.getId())
                .itineraryId(journal.getItineraryId())
                .city(journal.getCity())
                .country(journal.getCountry())
                .title(journal.getTitle())
                .description(journal.getDescription())
                .tags(journal.getTags())
                //.metadata(me)
                //.metadata(journal.getMetadata())
                .createdDate(journal.getCreatedDate())
                .modifiedDate(journal.getModifiedDate())
                .build();

    }


    /* ---------- helpers ---------- */

    /**
     * Trims leading and trailing whitespace from the input.
     * If {@code value} is {@code null} or the trimmed result is empty, this method returns {@code null}. Otherwise,
     * it returns the trimmed string.
     *
     * @param value the string to trim; may be {@code null}
     * @return the trimmed string, or {@code null} if the input was {@code null} or blank after trimming
     */
    private static String trimToNull(String value) {
        if (value == null) return null;

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Creates a mutable copy of the given list.
     * *
     * If {@code src} is {@code null}, this method returns a new empty {@link ArrayList}.
     * If {@code src} is non-null, it returns a new {@link ArrayList} containing the same elements (a shallow copy).
     * The returned list is never {@code null}.
     *
     * @param src the source list to copy; may be {@code null}
     * @return a new mutable list (never {@code null}); empty if {@code src} was {@code null}
     */
    private static List<String> copyToList(List<String> src) {
        return (src == null) ? new ArrayList<>() : new ArrayList<>(src);
    }
}