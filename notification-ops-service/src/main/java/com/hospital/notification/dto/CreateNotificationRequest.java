package com.hospital.notification.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNotificationRequest {

    private String type;
    private String title;
    private String message;
    private String targetWard;
    private String targetUserId;
    private String priority;
    private Long bedId;
    private Long patientId;
}
