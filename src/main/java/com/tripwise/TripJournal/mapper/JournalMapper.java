package com.tripwise.TripJournal.mapper;

import com.tripwise.TripJournal.dto.MetadataDTO;
import com.tripwise.TripJournal.dto.requests.CreateJournalRequest;
import com.tripwise.TripJournal.dto.requests.UpdateJournalRequest;
import com.tripwise.TripJournal.dto.responses.JournalResponse;
import com.tripwise.TripJournal.model.Journal;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

import static org.springframework.data.mongodb.util.BsonUtils.asMap;

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
                .metadata(request.getMetadata())
                // metadata (gps + weather) is set by the service after persisting
                .createdDate(now)
                .modifiedDate(now)
                .build();
    }

    /* ---------- Metadata ---------- */
    private static MetadataDTO mapMetadata(Map<String, Object> meta) {
        if (meta == null || meta.isEmpty()) return null;

        Map<String, Object> gpsMap = toMap(meta.get("gps"));
        Map<String, Object> wxMap  = toMap(meta.get("weather"));

        MetadataDTO.GpsDTO gps = null;
        if (gpsMap != null) {
            Double lat = getAsDouble(gpsMap, "lat");
            if (lat == null) lat = getAsDouble(gpsMap, "latitude");   // your stored keys
            Double lon = getAsDouble(gpsMap, "lon");
            if (lon == null) lon = getAsDouble(gpsMap, "longitude");

            if (lat != null || lon != null) {
                gps = MetadataDTO.GpsDTO.builder().lat(lat).lon(lon).build();
            }
        }

        MetadataDTO.WeatherDTO wx = null;
        if (wxMap != null) {
            wx = MetadataDTO.WeatherDTO.builder()
                    .temperature(getAsDouble(wxMap, "temperature"))
                    .description(getAsString(wxMap, "description"))
                    .humidity(getAsInteger(wxMap, "humidity"))
                    .windSpeed(getAsDouble(wxMap, "windSpeed"))
                    .icon(getAsString(wxMap, "icon"))
                    .build();
        }

        if (gps == null && wx == null) return null;
        return MetadataDTO.builder().gps(gps).weather(wx).build();
    }

    private static Map<String, Object> toMap(Object o) {
        return (o instanceof Map) ? (Map<String, Object>) o : null;
    }
    private static Double getAsDouble(Map<String, Object> m, String k) {
        Object v = m.get(k);
        if (v == null) return null;
        if (v instanceof Double) return (Double) v;
        if (v instanceof Float) return ((Float) v).doubleValue();
        if (v instanceof Integer) return ((Integer) v).doubleValue();
        if (v instanceof Long) return ((Long) v).doubleValue();
        if (v instanceof String) try {
            return Double.valueOf((String) v);
        } catch (NumberFormatException ignore) {
        }
        return null;
    }

    private static String getAsString(Map<String, Object> m, String k) {
        Object v = m.get(k);
        return v == null ? null : String.valueOf(v);
    }

    private static Integer getAsInteger(Map<String, Object> m, String k) {
        Object v = m.get(k);
        if (v == null) return null;
        if (v instanceof Integer) return (Integer) v;
        if (v instanceof Long) return ((Long) v).intValue();
        if (v instanceof Double) return ((Double) v).intValue();
        if (v instanceof String) try {
            return Integer.valueOf((String) v);
        } catch (NumberFormatException ignore) {
        }
        return null;
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
            String newCity = trimToNull(request.getCity());
            if (!Objects.equals(newCity, targetJournal.getCity())) {
                targetJournal.setCity(newCity);
                locationChanged = true;
            }
        }
        if (request.getCountry() != null) {
            String newCountry = trimToNull(request.getCountry());
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
                .mediaIds(journal.getMediaIds())
                .metadata(mapMetadata(journal.getMetadata()))
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