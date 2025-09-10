package com.tripwise.TripJournal.service;

import com.tripwise.TripJournal.dto.MetadataDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;


/**
 * ================================================================
 * Package Name: com.tripwise.TripJournal.service
 * Author      : Ochwada-GMK
 * Project Name: tripjournal
 * Date        : Thursday,  28.Aug.2025 | 18:05
 * Description :  Handles auto metadata enrichment + small helpers.
 * ================================================================
 */
@Component
@RequiredArgsConstructor
public class JournalEnricher {
    private final MetadataService metadataService;

    /**
     * Build auto metadata (gps + weather) from city/country.
     */

    public Map<String, Object> buildAutoMetadata(String city, String country) {

        Map<String, Object> auto = new HashMap<>();

        if (city == null || city.isBlank()) return auto;

        try {
            MetadataDTO metadataDTO = metadataService.buildMetadata(city, country);

            if (metadataDTO != null) {
                if (metadataDTO.getGps() != null) {
                    Map<String, Object> gps = new HashMap<>();
                    gps.put("latitude", metadataDTO.getGps().getLat());
                    gps.put("longitude", metadataDTO.getGps().getLon());
                    auto.put("gps", gps);
                }
                if (metadataDTO.getWeather() != null) {
                    Map<String, Object> weather = new LinkedHashMap<>();

                    weather.put("temperature", metadataDTO.getWeather().getTemperature());
                    weather.put("description", metadataDTO.getWeather().getDescription());
                    weather.put("humidity", metadataDTO.getWeather().getHumidity());
                    weather.put("windSpeed", metadataDTO.getWeather().getWindSpeed());
                    weather.put("icon", metadataDTO.getWeather().getIcon());
                    auto.put("weather", weather);

                }
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
        return auto;
    }

    /**
     * Keep only auto-enriched parts from an existing metadata map.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> extractAutoPortion(Map<String, Object> metadata) {
        if (metadata == null) return new HashMap<>();
        Map<String, Object> out = new HashMap<>();

        if (metadata.containsKey("gps")) {
            out.put("gps",
                    (Map<String, Object>) metadata.get("gps")
            );
        }

        if (metadata.containsKey("weather")) {
            out.put("weather",
                    (Map<String, Object>) metadata.get("weather"));
        }
        return out;
    }

    /**
     * User metadata overrides auto on conflicts.
     */
    public Map<String, Object> mergeMetadata(
            Map<String, Object> auto,
            Map<String, Object> user) {

        Map<String, Object> merged = new LinkedHashMap<>();

        if (auto != null) merged.putAll(auto);
        if (user != null) merged.putAll(user);

        return merged;
    }

    /** Safe copy list (handles null). */
    public <T> List<T> safeCopy(List<T> list) {

        return (list == null) ? null : new ArrayList<>(list);
    }

    private static <T> List<T> nz(List<T> v) {
        return v == null ? new ArrayList<>() : new ArrayList<>(v);
    }

    public static String firstOrNull(List<String> v) {
        return (v != null && !v.isEmpty()) ? v.get(0) : null;
    }


    /** Case-insensitive "contains" regex for search. */
    public String containsRegex(String term) {

        if (term == null || term.isBlank()) return ".*";
        return ".*" + java.util.regex.Pattern.quote(term.trim()) + ".*";
    }


}
