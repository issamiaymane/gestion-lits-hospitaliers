package com.hospital.dashboard.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientOverviewDTO {

    private int totalAdmitted;
    private int inTransfer;
    private int dischargedToday;
    private int emergencies;
    private Map<String, Object> wardDistribution;
}
