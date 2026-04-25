package com.hospital.patient.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientFlowStatisticsDTO {

    private long totalPatients;
    private long admitted;
    private long inTransfer;
    private long discharged;
    private long emergency;
    private Map<String, Long> admissionsByWard;
    private double averageStayDuration;
}
