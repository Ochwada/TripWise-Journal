package com.tripwise.TripJournal.service;

import com.tripwise.TripJournal.dto.MetadataDTO;
import com.tripwise.TripJournal.service.client.WeatherClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * ================================================================
 * Package Name: com.tripwise.TripJournal.service
 * Author      : Ochwada-GMK
 * Project Name: tripjournal
 * Date        : Thursday,  28.Aug.2025 | 17:08
 * Description : Service responsible for building enriched metadata (GPS coordinates + current weather) for a given
 * location. Wraps the WeatherClient for geocoding and weather retrieval, and maps results into a{@link MetadataDTO}.
 * ================================================================
 */
@Service
@RequiredArgsConstructor
public class MetadataService {
    private final WeatherClient weatherClient;


    /**
     * Builds a metadata object containing geocoded coordinates and current weather for the specified location.
     *
     * @param city        the name of the city to geocode (e.g., "Berlin")
     * @param countryCode optional ISO 3166 country code (e.g., "DE"). May be {@code null}.
     * @return a {@link MetadataDTO} with GPS coordinates and weather
     * @throws RuntimeException if the location cannot be found or weather data cannot be retrieved
     */
    public MetadataDTO buildMetadata(String city, String countryCode) {

        WeatherClient.Gps gps = weatherClient.geocode(city, countryCode);
        WeatherClient.Weather wx = weatherClient.fetchCurrent(gps.lat(), gps.lon());

        return MetadataDTO.builder()
                .gps(MetadataDTO.GpsDTO.builder()
                        .lat(gps.lat())
                        .lon(gps.lon())
                        .build())
                .weather(MetadataDTO.WeatherDTO.builder()
                        .temperature(wx.temperature())
                        .description(wx.description())
                        .humidity(wx.humidity())
                        .windSpeed(wx.windSpeed())
                        .icon(wx.icon())
                        .build())
                .build();
    }
}
