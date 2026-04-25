package com.hospital.dashboard.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BedOverviewDTO {

    private int totalBeds;
    private int freeBeds;
    private int occupiedBeds;
    private int reservedBeds;
    private int cleaningBeds;
    private double occupancyRate;
    private Map<String, Object> wardBreakdown;
}
