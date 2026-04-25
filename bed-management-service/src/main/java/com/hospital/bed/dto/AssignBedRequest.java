package com.hospital.bed.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignBedRequest {

    private Long bedId;
    private Long patientId;
    private String assignedBy;
    private String reason;
}
