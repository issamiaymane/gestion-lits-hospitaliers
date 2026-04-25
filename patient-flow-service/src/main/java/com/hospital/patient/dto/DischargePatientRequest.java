package com.hospital.patient.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DischargePatientRequest {

    private Long patientId;
    private String notes;
}
