package com.tripwise.TripJournal.service.client;

import com.tripwise.TripJournal.dto.MediaSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

/**
 * ================================================================
 * Package Name: com.tripwise.TripJournal.service.client
 * Author      : Ochwada-GMK
 * Project Name: tripjournal
 * Date        : Thursday,  04.Sept.2025 | 12:32
 * Description : Client for calling the tripmedia service from tripjournal.
 * ================================================================
 */

/**
 * This component delegates media-related operations (thumbnail generation, asset refresh, deletion) to the
 * <code>tripmedia</code> service over HTTP.
 * It assumes service-to-service communication inside Docker using the network alias <code>http://tripmedia:9096</code>.
 *
 * <h3>Authentication</h3>
 * Requests are expected to include the user's <em>JWT</em> via the <code>Authorization: Bearer &lt;token&gt;</code> header.
 * Configure a {@link RestTemplate} bean with an interceptor that propagates the current request's JWT (see your <code>
 *     RestTemplateConfig
 *
 * <h3>Endpoints used</h3>
 * <ul>
 *   <li><code>POST /media/thumbnail?journalId={id}</code></li>
 *   <li><code>POST /media/refresh?journalId={id}</code></li>
 *   <li><code>DELETE /media/delete?journalId={id}</code></li>
 * </ul>
 *
 * <h3>Typical responses</h3>
 * 200/201/202 for successful POSTs; 204 for DELETE. Non-2xx responses will raise a {@link RestClientException}.
 * Callers should handle failures without breaking the main workflow.
 */
@Component
@RequiredArgsConstructor
public class TripMediaClient {
    /** Base URL of the tripmedia service (Docker network hostname + port). */
    private final RestTemplate restTemplate;

    private final WebClient webClient;

    private static final String BASE = "http://tripmedia:9096";

    @Value("${tripmedia.base-url:https://tripmedia:9096}")
    private String mediaBase;


    /**
     * Request thumbnail generation for the given journal.
     *
     * @param journalId the journal identifier; must not be {@code null}
     * @throws RestClientException if the request fails or returns a non-2xx status
     */
    public void generateThumbnail(String journalId) {
        restTemplate.postForLocation(BASE + "/media/thumbnail?journalId=" + journalId, null);
    }


    /**
     * Refresh/rehydrate media assets for the given journal (e.g., rebuild derived files, update captions/EXIF-derived fields).
     *
     * @param journalId the journal identifier; must not be {@code null}
     * @throws RestClientException if the request fails or returns a non-2xx status
     */
    public void refreshAssets(String journalId) {
        restTemplate.postForLocation(BASE + "/media/refresh?journalId=" + journalId, null);
    }

    /**
     * Delete all media assets associated with the given journal.
     *
     * @param journalId the journal identifier; must not be {@code null}
     * @throws RestClientException if the request fails or returns a non-2xx status
     */
    public void deleteAssets(String journalId) {
        restTemplate.delete(BASE + "/media/delete?journalId=" + journalId);
    }

    public List<MediaSummary> batch(String bearerToken, List<String> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        try {
            return webClient.post()
                    .uri(mediaBase + "/media/batch")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(h -> h.setBearerAuth(extractToken(bearerToken)))
                    .bodyValue(ids)
                    .retrieve()
                    .bodyToFlux(MediaSummary.class)
                    .collectList()
                    .block();
        } catch (Exception e) {
            return Collections.emptyList(); // fail-soft on expansion
        }
    }

    private String extractToken(String bearer) {
        return bearer != null && bearer.startsWith("Bearer ") ? bearer.substring(7) : bearer;
    }

}
