package com.hospital.patient.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "patient_transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long patientId;

    private String fromWard;

    @Column(nullable = false)
    private String toWard;

    private LocalDateTime transferDate;

    private String reason;

    private String authorizedBy;

    @Enumerated(EnumType.STRING)
    private TransferStatus status;
}
