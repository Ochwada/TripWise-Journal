package com.tripwise.TripJournal.dto.responses;

import lombok.*;

import java.util.*;

/**
 * ================================================================
 * Package Name: com.tripwise.TripJournal.dto.responses
 * Author      : Ochwada-GMK
 * Project Name: tripjournal
 * Date        : Thursday,  28.Aug.2025 | 15:04
 * Description :
 * ================================================================
 */
@Data
@Builder
public class PageResponse<T> {
    private List<T> description;
    private int pageNumber;
    private int pageSize;
    private long totalPages;
}
