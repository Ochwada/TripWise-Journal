package com.tripwise.TripJournal.service;

import com.tripwise.TripJournal.dto.requests.CreateJournalRequest;
import com.tripwise.TripJournal.dto.requests.UpdateJournalRequest;
import com.tripwise.TripJournal.model.Journal;
import com.tripwise.TripJournal.repository.JournalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

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
        Map<String, Object> auto   = enricher.buildAutoMetadata(req.getCity(), req.getCountry());
        Map<String, Object> merged = enricher.mergeMetadata(auto, req.getMetadata());

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

        return repository.save(journal);
    }


    /**
     * Full update (PUT semantics). If city/country changed, auto metadata is re-built.
     * User-provided metadata still wins on key conflicts.
     */
    public Journal updateJournal(String userId, String id, UpdateJournalRequest req) {
        Journal existing = getJournal(userId, id);

        boolean locationChanged =
                !Objects.equals(existing.getCity(), req.getCity()) ||
                        !Objects.equals(existing.getCountry(), req.getCountry());

        Map<String, Object> baseAuto = locationChanged
                ? enricher.buildAutoMetadata(req.getCity(), req.getCountry())
                : enricher.extractAutoPortion(existing.getMetadata());

        Map<String, Object> merged = enricher.mergeMetadata(baseAuto, req.getMetadata());

        existing.setItineraryId(req.getItineraryId());
        existing.setCity(req.getCity());
        existing.setCountry(req.getCountry());
        existing.setTitle(req.getTitle());
        existing.setDescription(req.getDescription());
        existing.setTags(enricher.safeCopy(req.getTags()));
        existing.setMetadata(merged.isEmpty() ? null : merged);
        existing.setModifiedDate(Instant.now());

        return repository.save(existing);
    }

    /** Delete a journal owned by the user. */
    public void deleteJournal(String userId, String id) {
        Journal existing = getJournal(userId, id); // enforces ownership/404
        repository.deleteByIdAndUserId(existing.getId(), userId);
    }

    /** Search by title (case-insensitive regex), scoped to the user (paginated). */
    public Page<Journal> searchByTitle(String userId, String term, Pageable pageable) {
        String rx = enricher.containsRegex(term);
        return repository.searchByUserAndTitle(userId, rx, pageable);
    }


}
