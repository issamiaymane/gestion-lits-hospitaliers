package com.hospital.dashboard.controller;

import com.hospital.dashboard.dto.DashboardDTO;
import com.hospital.dashboard.dto.DashboardKpisDTO;
import com.hospital.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @Operation(summary = "Get full dashboard data")
    public ResponseEntity<DashboardDTO> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }

    @GetMapping("/kpis")
    @Operation(summary = "Get KPIs only")
    public ResponseEntity<DashboardKpisDTO> getKpis() {
        return ResponseEntity.ok(dashboardService.getKpis());
    }
}
