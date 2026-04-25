package com.hospital.patient.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private LocalDate dateOfBirth;

    private String gender;

    @Column(unique = true)
    private String nationalId;

    private String contactPhone;

    private String emergencyContact;

    @Enumerated(EnumType.STRING)
    private PatientStatus currentStatus;

    @Column(nullable = true)
    private String currentWard;

    @Column(nullable = true)
    private LocalDateTime admissionDate;

    @Column(nullable = true)
    private LocalDateTime dischargeDate;

    @Column(columnDefinition = "TEXT")
    private String medicalNotes;
}
