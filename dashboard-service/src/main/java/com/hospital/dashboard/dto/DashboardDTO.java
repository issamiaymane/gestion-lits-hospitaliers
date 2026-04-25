package com.hospital.dashboard.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDTO {

    private LocalDateTime timestamp;
    private BedOverviewDTO bedOverview;
    private PatientOverviewDTO patientOverview;
    private Object capacityOverview;
    private List<Object> recentNotifications;
    private List<String> alerts;
}
