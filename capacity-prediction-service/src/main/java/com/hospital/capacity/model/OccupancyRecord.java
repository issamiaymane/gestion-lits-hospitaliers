package com.hospital.capacity.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "occupancy_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OccupancyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ward;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Integer hour;

    @Column(nullable = false)
    private int totalBeds;

    @Column(nullable = false)
    private int occupiedBeds;

    @Column(nullable = false)
    private double occupancyRate;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    protected void onCreate() {
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
        if (totalBeds > 0) {
            occupancyRate = (double) occupiedBeds / totalBeds * 100.0;
        }
    }
}
