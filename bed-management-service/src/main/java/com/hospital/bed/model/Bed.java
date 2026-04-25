package com.hospital.bed.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "beds")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String bedNumber;

    private String ward;

    private String roomNumber;

    private Integer floor;

    @Enumerated(EnumType.STRING)
    private BedType bedType;

    @Enumerated(EnumType.STRING)
    private BedStatus status;

    private Long currentPatientId;

    private LocalDateTime lastStatusChange;

    private String notes;

    @PrePersist
    @PreUpdate
    public void updateLastStatusChange() {
        this.lastStatusChange = LocalDateTime.now();
    }
}
