package com.tripwise.TripJournal.repository;

import com.tripwise.TripJournal.dto.responses.JournalResponse;
import com.tripwise.TripJournal.model.Journal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

/**
 * ================================================================
 * Package Name: com.tripwise.TripJournal.repository
 * Author      : Ochwada-GMK
 * Project Name: tripjournal
 * Date        : Thursday,  28.Aug.2025 | 12:19
 * Description : Repository interface for managing {@link Journal} entities in MongoDB.
 * - Extends {@link MongoRepository} to provide CRUD operations as well as custom query methods for journal entries.
 * ================================================================
 */
public interface JournalRepository extends MongoRepository<Journal, String> {

    /**
     * Retrieves a page of journals owned by the given user.
     *
     * @param userId   the owner user ID
     * @param pageable pagination information (page number, size, sort)
     * @return a page of {@link Journal} documents
     */
    Page<Journal> findByUserId(String userId, Pageable pageable);


    /**
     * Retrieves a single journal by its ID, scoped to a specific user.
     *
     * @param id     the journal ID
     * @param userId the expected owner user ID
     * @return an {@link Optional} containing the journal if found and owned by the user; empty otherwise
     */
    Optional<Journal> findByIdAndUserId(String id, String userId);

    /**
     * Case-insensitive title search for journals belonging to a user.
     * <p>
     * Uses a MongoDB regex query with the {@code i} option for case-insensitive matching. Pass an anchored pattern
     * (e.g., {@code ^hike}) to search from the start of the title.
     *
     * @param userId     the owner user ID
     * @param titleRegex a regular expression to match against {@code title}
     * @param pageable   pagination information
     * @return a page of matching {@link Journal} documents
     */
    @Query(value = "{ 'userId': ?0, 'title': { $regex: ?1, $options: 'i' } }")
    Page<Journal> searchByUserAndTitle(String userId, String titleRegex, Pageable pageable);


    /**
     * Deletes a journal by ID if (and only if) it belongs to the given user.
     *
     * @param id     the journal ID
     * @param userId the owner user ID
     */
    long deleteByIdAndUserId(String id, String userId);


}
