package com.hospital.notification.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {

    private Long id;
    private String type;
    private String title;
    private String message;
    private String targetWard;
    private String targetUserId;
    private String priority;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private Long bedId;
    private Long patientId;
}
