package com.hospital.bed.dto;

import com.hospital.bed.model.BedStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBedStatusRequest {

    private BedStatus status;
    private Long patientId;
    private String notes;
}
