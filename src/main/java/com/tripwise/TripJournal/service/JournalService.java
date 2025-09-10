package com.tripwise.TripJournal.service;

import com.tripwise.TripJournal.dto.requests.CreateJournalRequest;
import com.tripwise.TripJournal.dto.requests.UpdateJournalRequest;
import com.tripwise.TripJournal.dto.responses.JournalResponse;
import com.tripwise.TripJournal.mapper.JournalMapper;
import com.tripwise.TripJournal.model.Journal;
import com.tripwise.TripJournal.repository.JournalRepository;
import com.tripwise.TripJournal.service.client.TripMediaClient;
import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Meta;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.server.ResponseStatusException;


import java.time.Instant;
import java.util.*;

import static org.springframework.http.HttpStatus.NOT_FOUND;


/**
 * ================================================================
 * Package Name: com.tripwise.TripJournal.service
 * Author      : Ochwada-GMK
 * Project Name: tripjournal
 * Date        : Thursday,  28.Aug.2025 | 17:35
 * Description :  Service for managing travel journal entries.
 * Provides CRUD operations and automatically enriches entries with GPS + weather metadata.
 * ================================================================
 */
@Service
@RequiredArgsConstructor
public class JournalService {
    private final JournalRepository repository;
    private final JournalEnricher enricher;
    private final TripMediaClient tripMediaClient;
    private final ServiceHelpers helpers;
    private final JournalMapper mapper;
    private final MetadataService metadataService;


    @Value("${journal.enrichment.enabled:true}")
    private boolean enrichmentEnabled;

    @Value("${journal.media-callbacks.enabled:true}")
    private boolean mediaCallbacksEnabled;

    /** List journals for the authenticated user (paginated). */
    public List<JournalResponse> findAllJournals(String userId) {
        return repository.findByUserId(userId, Pageable.unpaged())
                .map(mapper::toResponse)
                .getContent();
    }

    /** Get a single journal owned by the user. */
    public Journal findJournalById(String journalId) {
        return repository.findById(journalId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Journal not found: " + journalId));

    }


    /** Get a single journal owned by the user. */
    public JournalResponse getJournal(String userId, String id) {
        return repository.findByIdAndUserId(id, userId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Journal not found: " + id));

    }


    /** Create a journal; user metadata overrides auto-enriched keys on conflict. */
    public Journal createJournal(String userId, CreateJournalRequest req) {
        Map<String, Object> auto = helpers.tryAutoMetadata(req.getCity(), req.getCountry());
        Map<String, Object> merged = enricher.mergeMetadata(auto, req.getMetadata());


//        Map<String, Object> auto   = enricher.buildAutoMetadata(req.getCity(), req.getCountry());
//        Map<String, Object> merged = enricher.mergeMetadata(auto, req.getMetadata());

        Journal journal = Journal.builder()
                .userId(userId)
                .itineraryId(req.getItineraryId())
                .title(req.getTitle())
                .description(req.getDescription())
                .city(req.getCity())
                .country(req.getCountry())
                .mediaIds(enricher.safeCopy(req.getMediaIds()))
                .tags(enricher.safeCopy(req.getTags()))
                .metadata(merged.isEmpty() ? null : merged)
                .createdDate(Instant.now())
                .modifiedDate(Instant.now())
                .build();

        Journal savedJournal = repository.save(journal);

        helpers.safeCall(() -> tripMediaClient.generateThumbnail(savedJournal.getId()) );

        return savedJournal;
    }


    /**
     * Full update (PUT semantics). If city/country changed, auto metadata is re-built.
     * User-provided metadata still wins on key conflicts.
     */
    @Transactional
    public JournalResponse updateJournal(String userId, String id, UpdateJournalRequest req) {
        Journal existing = getJournalEntity(userId, id);

        boolean locationChanged =
                !Objects.equals(existing.getCity(), req.getCity()) ||
                !Objects.equals(existing.getCountry(), req.getCountry());

        Map<String, Object> baseAuto = locationChanged
                ? helpers.tryAutoMetadata(req.getCity(), req.getCountry())
                : enricher.extractAutoPortion(existing.getMetadata());

        Map<String, Object> merged = enricher.mergeMetadata(baseAuto, req.getMetadata());
        boolean titleChanged = !Objects.equals(existing.getTitle(), req.getTitle());

        // Compare list fields defensively (null-safe)
        boolean coverChanged = req.getCoverMediaId() != null
                && !Objects.equals(existing.getCoverMediaId(), req.getCoverMediaId());

        boolean mediaChanged = req.getMediaIds() != null
                && !Objects.equals(existing.getMediaIds(), req.getMediaIds());

        /**existing.setItineraryId(req.getItineraryId());
        existing.setCity(req.getCity());
        existing.setCountry(req.getCountry());
        existing.setTitle(req.getTitle());
        existing.setDescription(req.getDescription());
        existing.setTags(enricher.safeCopy(req.getTags()));
        existing.setMetadata(merged.isEmpty() ? null : merged);
        existing.setModifiedDate(Instant.now());

        Journal saved = repository.save(existing);

        if (locationChanged || titleChanged) {
            helpers.safeCall(() -> tripMediaClient.refreshAssets(saved.getId()));
        }

        return mapper.toResponse(saved);*/
        // Scalar updates
        if (req.getItineraryId() != null) existing.setItineraryId(req.getItineraryId());
        if (req.getCity() != null)        existing.setCity(req.getCity());
        if (req.getCountry() != null)     existing.setCountry(req.getCountry());
        if (req.getTitle() != null)       existing.setTitle(req.getTitle());
        if (req.getDescription() != null) existing.setDescription(req.getDescription());

        // List updates (null-safe copies only when provided)
        //if (req.getCoverMediaId() != null) existing.setCoverMediaId(nonNullCopy(req.getCoverMediaId()));

        if (req.getMediaIds() != null)     existing.setMediaIds(enricher.safeCopy(req.getMediaIds()));
        if (req.getTags() != null)         existing.setTags(enricher.safeCopy(req.getTags()));

        // Metadata (keep null when empty)
        if (req.getMetadata() != null || locationChanged) {
            existing.setMetadata(merged.isEmpty() ? null : merged);
        }

        existing.setModifiedDate(Instant.now());

        Journal saved = repository.save(existing);

        // If location/title/cover/media changed, refresh derived media
        if (locationChanged || titleChanged || coverChanged || mediaChanged) {
            helpers.safeCall(() -> tripMediaClient.refreshAssets(saved.getId()));
        }

        return mapper.toResponse(saved);

    }

    private Journal getJournalEntity(String userId, String id) {
        return repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Journal not found: " + id));
    }

    @Transactional
    public JournalResponse patchJournal(String userId, String id, Map<String, Object> updates) {
        Journal existing = getJournalEntity(userId, id); // returns Journal entity

        if (updates.containsKey("title")) {
            existing.setTitle((String) updates.get("title"));
        }
        if (updates.containsKey("description")) {
            existing.setDescription((String) updates.get("description"));
        }
        if (updates.containsKey("tags")) {
            existing.setTags((List<String>) updates.get("tags"));
        }
        if (updates.containsKey("metadata")) {
            existing.setMetadata((Map<String, Object>) updates.get("metadata"));
        }

        existing.setModifiedDate(Instant.now());
        Journal saved = repository.save(existing);

        return mapper.toResponse(saved);
    }



    /** Delete a journal owned by the user. */
    @Transactional
    public void deleteJournal(String userId, String id) {
        Journal existing = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal not found: " + id));

        helpers.safeCall(() -> tripMediaClient.deleteAssets(existing.getId()));

        long deleted = repository.deleteByIdAndUserId(existing.getId(), userId);
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal not found: " + id);
        }
    }


    /** Search by title (case-insensitive regex), scoped to the user (paginated). */
    public Page<Journal> searchByTitle(String userId, String term, Pageable pageable) {
        String rx = enricher.containsRegex(term);
        return repository.searchByUserAndTitle(userId, rx, pageable);
    }


}
