package com.tripwise.TripJournal.dto;

import lombok.*;

/**
 * ================================================================
 * Package Name: com.tripwise.TripJournal.dto
 * Author      : Ochwada-GMK
 * Project Name: tripjournal
 * Date        : Thursday,  28.Aug.2025 | 15:14
 * Description : Read-only metadata returned to clients (auto-fetched).
 * ================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetadataDTO {

    private GpsDTO gps;
    private WeatherDTO weather;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GpsDTO {
        private Double lat;
        private Double lon;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeatherDTO {
        private Double temperature;   // °C
        private String description;   // "Sunny", "Light rain", ...
        private Integer humidity;     // %
        private Double windSpeed;     // m/s or km/h (be consistent)
        private String icon;          // optional: OpenWeather icon code
    }
}
