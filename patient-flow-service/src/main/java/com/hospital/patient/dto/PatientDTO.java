package com.hospital.patient.dto;

import com.hospital.patient.model.PatientStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private String nationalId;
    private String contactPhone;
    private String emergencyContact;
    private PatientStatus currentStatus;
    private String currentWard;
    private LocalDateTime admissionDate;
    private LocalDateTime dischargeDate;
    private String medicalNotes;
}
