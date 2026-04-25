package com.hospital.notification.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationStatsDTO {

    private long total;
    private long pending;
    private long sent;
    private long read;
    private Map<String, Long> byType;
    private Map<String, Long> byPriority;
}
