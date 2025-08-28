package com.tripwise.TripJournal.dto.requests;

import lombok.*;

/**
 * ================================================================
 * Package Name: com.tripwise.TripJournal.dto
 * Author      : Ochwada-GMK
 * Project Name: tripjournal
 * Date        : Thursday,  28.Aug.2025 | 13:37
 * Description :
 * ================================================================
 */
@Data
public class SearchJournalRequest {
    private String query; // matches title (regex)
    private int page = 0;
    private int size = 20;
    private String sort = "createdAt,desc"; // field,direction
}
