package com.tripwise.TripJournal.service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * ================================================================
 * Package Name: com.tripwise.TripJournal.service
 * Author      : Ochwada-GMK
 * Project Name: tripjournal
 * Date        : Thursday,  28.Aug.2025 | 16:21
 * Description : A Spring-managed client for interacting with the OpenWeather API. Provides methods for geocoding
 * (city → coordinates) and fetching current weather  data for given coordinates. Uses Spring WebClient or non-blocking HTTP calls.
 * ================================================================
 */
@Component
public class WeatherClient {

    private final WebClient webClient;

    @Value("${openweather.apiKey}")
    private String apiKey;

    /**
     * Constructs a WeatherClient with a shared {@link WebClient.Builder}, ensuring connection pooling and common
     * configuration across the app.
     *
     * @param builder the Spring-injected WebClient builder
     */
    public WeatherClient(WebClient.Builder builder) {
        // use DI so it can share connection pool and timeouts
        this.webClient = builder
                .baseUrl("https://api.openweathermap.org")
                .build();
    }

    /**
     * Resolves a city (optionally with a country code) into geographic coordinates (latitude, longitude) using the
     * OpenWeather geocoding API.
     *
     * @param city        the city name (e.g., "Berlin")
     * @param countryCode optional ISO 3166-1 alpha-2 code (e.g., "DE");  may be {@code null}
     * @return a {@link Gps} record containing latitude and longitude
     * @throws RuntimeException if no matching location is found
     */
    public Gps geocode(String city, String countryCode) {
        List<GeoRes> res = webClient.get()
                .uri(uri -> uri.path("/geo/1.0/direct")
                        .queryParam("q", countryCode == null ? city : city + "," + countryCode)
                        .queryParam("limit", 1)
                        .queryParam("appid", apiKey)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(GeoRes.class)
                .collectList()
                .block();

        if (res == null || res.isEmpty()) throw new RuntimeException("Location not found");
        return new Gps(res.get(0).lat, res.get(0).lon);
    }

    /**
     * Fetches the current weather for given coordinates using the OpenWeather "Current Weather Data" API.
     *
     * @param lat latitude of the location
     * @param lon longitude of the location
     * @return a {@link Weather} record containing temperature, humidity,  wind speed, description, and optional icon code
     * @throws RuntimeException if the weather data cannot be retrieved
     */
    public Weather fetchCurrent(double lat, double lon) {
        WxRes res = webClient.get()
                .uri(uri -> uri.path("/data/2.5/weather")
                        .queryParam("lat", lat)
                        .queryParam("lon", lon)
                        .queryParam("units", "metric")
                        .queryParam("appid", apiKey)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(WxRes.class)
                .block();

        if (res == null) throw new RuntimeException("Weather fetch failed");

        String desc = (res.weather != null && !res.weather.isEmpty()) ? res.weather.get(0).description : null;
        String icon = (res.weather != null && !res.weather.isEmpty()) ? res.weather.get(0).icon : null;

        return new Weather(
                res.main != null ? res.main.temp : null,
                desc,
                res.main != null ? res.main.humidity : null,
                res.wind != null ? res.wind.speed : null,
                icon
        );
    }

    /* ---------------------------
    tiny DTOs for OpenWeather
    --------------------------- */
    record GeoRes(
            String name, double lat,
            double lon, String country,
            String state) {}

    record WxRes(Main main, List<WItem> weather, Wind wind) {

        record Main(Double temp, Integer humidity) {}
        record WItem(String main, String description, String icon) {}
        record Wind(Double speed) {}
    }

    /* --- local simple models --- */
    public record Gps(Double lat, Double lon) {}

    public record Weather(
            Double temperature, String description,
            Integer humidity, double windSpeed,
            String icon) {}
}
