package com.hospital.bed.dto;

import com.hospital.bed.model.BedStatus;
import com.hospital.bed.model.BedType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BedDTO {

    private Long id;
    private String bedNumber;
    private String ward;
    private String roomNumber;
    private Integer floor;
    private BedType bedType;
    private BedStatus status;
    private Long currentPatientId;
    private LocalDateTime lastStatusChange;
    private String notes;
}
