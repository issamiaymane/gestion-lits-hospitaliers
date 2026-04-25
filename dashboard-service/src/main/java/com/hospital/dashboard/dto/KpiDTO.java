package com.hospital.dashboard.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KpiDTO {

    private String name;
    private double value;
    private String unit;
    private String trend;
    private String status;
}
