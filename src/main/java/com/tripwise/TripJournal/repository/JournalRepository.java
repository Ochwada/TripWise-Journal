package com.tripwise.TripJournal.repository;

import com.tripwise.TripJournal.model.Journal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

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
     * Finds a paginated list of journal entries for a given user, where the activity title matches a case-insensitive
     * regular expression.
     *
     * @param userId     the ID of the user who owns the journals
     * @param titleRegex the regular expression to match against activity titles (case-insensitive)
     * @param pageable   pagination information
     * @return a {@link Page} of {@link Journal} entries that match the query
     */
    Page<Journal> findByUserIdAndTitleRegexIgnoreCase(
            String userId,
            String titleRegex,
            Pageable pageable
    );


    /**
     * Finds a paginated list of journal entries for a specific user.
     *
     * @param userId   the ID of the user who owns the journals
     * @param pageable pagination information
     * @return a {@link Page} of {@link Journal} entries belonging to the user
     */
    Page<Journal> findByUserId(String userId, Pageable pageable);
}
