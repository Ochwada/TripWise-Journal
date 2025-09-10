package com.tripwise.TripJournal.dto;

import lombok.*;

/**
 * ================================================================
 * Package Name: com.tripwise.TripJournal.dto
 * Author      : Ochwada-GMK
 * Project Name: tripjournal
 * Date        : Wednesday,  10.Sept.2025 | 09:55
 * Description :
 * ================================================================
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MediaSummary {
    private String id;
    private String fileName;
    private String mimeType;
    private Long bytes;
    private Integer width;
    private Integer height;
    private String cdnUrl;      // may be null if not public
    private String storageKey;  // for building public URL in UI
}
