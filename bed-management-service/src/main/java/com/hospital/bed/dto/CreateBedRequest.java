package com.hospital.bed.dto;

import com.hospital.bed.model.BedType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBedRequest {

    private String bedNumber;
    private String ward;
    private String roomNumber;
    private Integer floor;
    private BedType bedType;
    private String notes;
}
