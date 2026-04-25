package com.hospital.capacity.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OverallCapacityDTO {

    private int totalBeds;
    private int occupiedBeds;
    private int freeBeds;
    private double overallOccupancyRate;
    private List<WardCapacityDTO> wardCapacities;
    private List<CapacityPredictionDTO> predictions;
}
