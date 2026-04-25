package com.hospital.capacity.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WardCapacityDTO {

    private String ward;
    private int totalBeds;
    private int occupiedBeds;
    private int freeBeds;
    private double occupancyRate;
}
