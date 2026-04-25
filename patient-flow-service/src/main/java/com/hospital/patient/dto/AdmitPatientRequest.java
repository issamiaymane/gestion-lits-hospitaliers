package com.hospital.patient.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdmitPatientRequest {

    private Long patientId;
    private String ward;
    private String medicalNotes;
}
