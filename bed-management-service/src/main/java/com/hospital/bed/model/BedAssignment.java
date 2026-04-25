package com.hospital.bed.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "bed_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BedAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long bedId;

    @Column(nullable = false)
    private Long patientId;

    private LocalDateTime assignedAt;

    private LocalDateTime releasedAt;

    private String assignedBy;

    private String reason;
}
