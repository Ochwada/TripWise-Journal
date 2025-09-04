package com.tripwise.TripJournal.service;

import com.tripwise.TripJournal.dto.requests.CreateJournalRequest;
import com.tripwise.TripJournal.dto.requests.UpdateJournalRequest;
import com.tripwise.TripJournal.model.Journal;
import com.tripwise.TripJournal.repository.JournalRepository;
import com.tripwise.TripJournal.service.client.TripMediaClient;
import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;


import java.time.Instant;
import java.util.*;


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


    @Value("${journal.enrichment.enabled:true}")
    private boolean enrichmentEnabled;

    @Value("${journal.media-callbacks.enabled:true}")
    private boolean mediaCallbacksEnabled;

    /** List journals for the authenticated user (paginated). */
    public Page<Journal> findAllJournals(String userId, Pageable pageable) {
        return repository.findByUserId(userId, pageable);
    }

    /** Get a single journal owned by the user. */
    public Journal findJournalById(String journalId) {
        return repository.findById(journalId)
                .orElseThrow(() -> new NoSuchElementException("Journal with id: " + journalId + " not found"));
    }


    /** Get a single journal owned by the user. */
    public Journal getJournal(String userId, String id) {
        return repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NoSuchElementException("Journal not found: " + id));
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
                .city(req.getCity())
                .country(req.getCountry())
                .title(req.getTitle())
                .description(req.getDescription())
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
    public Journal updateJournal(String userId, String id, UpdateJournalRequest req) {
        Journal existing = getJournal(userId, id);

        boolean locationChanged =
                !Objects.equals(existing.getCity(), req.getCity()) ||
                !Objects.equals(existing.getCountry(), req.getCountry());
        Map<String, Object> baseAuto = locationChanged
                ? helpers.tryAutoMetadata(req.getCity(), req.getCountry())
                : enricher.extractAutoPortion(existing.getMetadata());

//        Map<String, Object> baseAuto = locationChanged
//                ? enricher.buildAutoMetadata(req.getCity(), req.getCountry())
//                : enricher.extractAutoPortion(existing.getMetadata());

        Map<String, Object> merged = enricher.mergeMetadata(baseAuto, req.getMetadata());

        boolean titleChanged = !Objects.equals(existing.getTitle(), req.getTitle());

        existing.setItineraryId(req.getItineraryId());
        existing.setCity(req.getCity());
        existing.setCountry(req.getCountry());
        existing.setTitle(req.getTitle());
        existing.setDescription(req.getDescription());
        existing.setTags(enricher.safeCopy(req.getTags()));
        existing.setMetadata(merged.isEmpty() ? null : merged);
        existing.setModifiedDate(Instant.now());

        Journal savedJournal = repository.save(existing);

        //  If location/title changed, refresh media artifacts (thumbnails, captions, etc.)
        if (locationChanged || titleChanged) {
            helpers.safeCall(() -> tripMediaClient.refreshAssets(savedJournal.getId()) );
        }

        return savedJournal;
    }

    /** Delete a journal owned by the user. */
    @Transactional
    public void deleteJournal(String userId, String id) {
        Journal existing = getJournal(userId, id); // enforces ownership/404



        // Ask media to remove associated files first (best-effort)
        helpers.safeCall(() -> tripMediaClient.deleteAssets(existing.getId()));

        repository.deleteByIdAndUserId(existing.getId(), userId);
    }

    /** Search by title (case-insensitive regex), scoped to the user (paginated). */
    public Page<Journal> searchByTitle(String userId, String term, Pageable pageable) {
        String rx = enricher.containsRegex(term);
        return repository.searchByUserAndTitle(userId, rx, pageable);
    }


}
