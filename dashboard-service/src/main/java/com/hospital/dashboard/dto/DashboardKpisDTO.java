package com.hospital.dashboard.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardKpisDTO {

    private List<KpiDTO> kpis;
    private LocalDateTime generatedAt;
}
