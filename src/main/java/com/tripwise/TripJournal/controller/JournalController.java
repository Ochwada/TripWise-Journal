package com.tripwise.TripJournal.controller;

import com.tripwise.TripJournal.dto.requests.CreateJournalRequest;
import com.tripwise.TripJournal.dto.requests.UpdateJournalRequest;
import com.tripwise.TripJournal.model.Journal;
import com.tripwise.TripJournal.service.JournalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;



/**
 * ================================================================
 * Package Name: com.tripwise.TripJournal.controller
 * Author      : Ochwada-GMK
 * Project Name: tripjournal
 * Date        : Monday,  01.Sept.2025 | 10:59
 * Description :
 * ================================================================
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/journals")
public class JournalController {

    private final JournalService service;
    private final  ControllerHelpers helpers;

    /** GET /journals — Fetch all journals for the user (paginated). */
    @GetMapping
    public Page<Journal> findAllJournals(
            Authentication authentication,
            @PageableDefault(sort="createdDate", size = 20) Pageable pageable, Sort sort) {

        String userId = helpers.resolveUserId(authentication);

        return service.findAllJournals(userId, pageable);
    }

    /** POST /journals — Create a new travel journal entry. */
    @PostMapping
    public ResponseEntity<Journal> createJournal(
            Authentication auth,
            @RequestBody @Valid CreateJournalRequest request){
        String userId = helpers.resolveUserId(auth);
        Journal created = service.createJournal(userId, request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    /** GET /journals/{id} — Retrieve a single journal by ID. */
    @GetMapping("/{id}")
    public Journal getJournal(Authentication auth, @PathVariable String id){

        String userId = helpers.resolveUserId(auth);
        return service.getJournal(userId, id);
    }

    /** PUT /journals/{id} — Update an existing journal entry. */
    @PutMapping("/{id}")
    public Journal updateJournal( Authentication auth,
                                  @PathVariable String id,
                                  @RequestBody @Valid UpdateJournalRequest request){

        String userId = helpers.resolveUserId(auth);
        return service.updateJournal(userId, id, request);
    }

    /** DELETE /journals/{id} — Delete a journal entry. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJournal(Authentication auth, @PathVariable String id){
        String userId = helpers.resolveUserId(auth);
        service.deleteJournal(userId, id);
        return ResponseEntity.noContent().build();
    }

    /** GET /journals/search?q=term — Search journals by title (case-insensitive). */
    @GetMapping("/search")
    public Page<Journal> searchJournal( Authentication auth,
                                        @RequestParam(name = "q", required = false) String q,
                                        @PageableDefault(sort = "createdDate", size = 20) Pageable pageable){
        String userId = helpers.resolveUserId(auth);

        return service.searchByTitle(userId, q, pageable);
    }

}
