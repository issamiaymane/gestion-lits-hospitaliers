package com.hospital.patient.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferPatientRequest {

    private Long patientId;
    private String toWard;
    private String reason;
    private String authorizedBy;
}
