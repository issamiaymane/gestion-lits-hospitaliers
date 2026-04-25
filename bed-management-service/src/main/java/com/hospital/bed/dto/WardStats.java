package com.hospital.bed.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WardStats {

    private String ward;
    private long total;
    private long free;
    private long occupied;
    private long reserved;
    private long cleaning;
    private double occupancyRate;
}
