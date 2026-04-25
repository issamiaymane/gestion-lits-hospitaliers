package com.hospital.capacity.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapacityPredictionDTO {

    private String ward;
    private double currentOccupancy;
    private double predictedOccupancy;
    private int predictedAvailableBeds;
    private String trend;
    private String riskLevel;
    private List<String> recommendations;
}
