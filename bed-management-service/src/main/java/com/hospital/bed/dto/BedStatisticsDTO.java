package com.hospital.bed.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BedStatisticsDTO {

    private long totalBeds;
    private long freeBeds;
    private long occupiedBeds;
    private long reservedBeds;
    private long cleaningBeds;
    private double occupancyRate;
    private Map<String, WardStats> statsByWard;
}
