package com.hospital.dashboard.service;

import com.hospital.dashboard.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class DashboardService {

    private final WebClient webClient;

    @Value("${services.bed-management-service.url}")
    private String bedServiceUrl;

    @Value("${services.patient-flow-service.url}")
    private String patientServiceUrl;

    @Value("${services.capacity-prediction-service.url}")
    private String capacityServiceUrl;

    @Value("${services.notification-ops-service.url}")
    private String notificationServiceUrl;

    public DashboardService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @SuppressWarnings("unchecked")
    public DashboardDTO getDashboard() {
        BedOverviewDTO bedOverview = fetchBedOverview();
        PatientOverviewDTO patientOverview = fetchPatientOverview();
        Object capacityOverview = fetchCapacityOverview();
        List<Object> recentNotifications = fetchRecentNotifications();
        List<String> alerts = generateAlerts(bedOverview, patientOverview);

        return DashboardDTO.builder()
                .timestamp(LocalDateTime.now())
                .bedOverview(bedOverview)
                .patientOverview(patientOverview)
                .capacityOverview(capacityOverview)
                .recentNotifications(recentNotifications)
                .alerts(alerts)
                .build();
    }

    public DashboardKpisDTO getKpis() {
        BedOverviewDTO bedOverview = fetchBedOverview();
        PatientOverviewDTO patientOverview = fetchPatientOverview();

        List<KpiDTO> kpis = new ArrayList<>();

        double occupancyRate = bedOverview.getOccupancyRate();
        kpis.add(KpiDTO.builder()
                .name("Occupancy Rate")
                .value(occupancyRate)
                .unit("%")
                .trend(occupancyRate > 75 ? "INCREASING" : "STABLE")
                .status(occupancyRate > 90 ? "CRITICAL" : occupancyRate > 75 ? "WARNING" : "GOOD")
                .build());

        kpis.add(KpiDTO.builder()
                .name("Available Beds")
                .value(bedOverview.getFreeBeds())
                .unit("beds")
                .trend("STABLE")
                .status(bedOverview.getFreeBeds() < 5 ? "CRITICAL" : bedOverview.getFreeBeds() < 15 ? "WARNING" : "GOOD")
                .build());

        kpis.add(KpiDTO.builder()
                .name("Total Admitted Patients")
                .value(patientOverview.getTotalAdmitted())
                .unit("patients")
                .trend("STABLE")
                .status("GOOD")
                .build());

        kpis.add(KpiDTO.builder()
                .name("Patients In Transfer")
                .value(patientOverview.getInTransfer())
                .unit("patients")
                .trend("STABLE")
                .status(patientOverview.getInTransfer() > 10 ? "WARNING" : "GOOD")
                .build());

        kpis.add(KpiDTO.builder()
                .name("Discharged Today")
                .value(patientOverview.getDischargedToday())
                .unit("patients")
                .trend("STABLE")
                .status("GOOD")
                .build());

        int cleaningBeds = bedOverview.getCleaningBeds();
        double turnaroundEstimate = cleaningBeds > 0 ? cleaningBeds * 0.5 : 0;
        kpis.add(KpiDTO.builder()
                .name("Bed Turnaround Time (est.)")
                .value(turnaroundEstimate)
                .unit("hours")
                .trend("STABLE")
                .status(turnaroundEstimate > 5 ? "WARNING" : "GOOD")
                .build());

        int totalPatients = patientOverview.getTotalAdmitted();
        double transferRate = totalPatients > 0
                ? (double) patientOverview.getInTransfer() / totalPatients * 100.0
                : 0.0;
        kpis.add(KpiDTO.builder()
                .name("Transfer Rate")
                .value(Math.round(transferRate * 10.0) / 10.0)
                .unit("%")
                .trend("STABLE")
                .status(transferRate > 15 ? "WARNING" : "GOOD")
                .build());

        return DashboardKpisDTO.builder()
                .kpis(kpis)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    @SuppressWarnings("unchecked")
    private BedOverviewDTO fetchBedOverview() {
        try {
            Map<String, Object> stats = webClient.get()
                    .uri(bedServiceUrl + "/api/beds/statistics")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (stats == null) {
                return emptyBedOverview();
            }

            return BedOverviewDTO.builder()
                    .totalBeds(toInt(stats.get("totalBeds")))
                    .freeBeds(toInt(stats.get("freeBeds")))
                    .occupiedBeds(toInt(stats.get("occupiedBeds")))
                    .reservedBeds(toInt(stats.get("reservedBeds")))
                    .cleaningBeds(toInt(stats.get("cleaningBeds")))
                    .occupancyRate(toDouble(stats.get("occupancyRate")))
                    .wardBreakdown((Map<String, Object>) stats.get("wardStatistics"))
                    .build();
        } catch (Exception e) {
            log.error("Error fetching bed overview: {}", e.getMessage());
            return emptyBedOverview();
        }
    }

    @SuppressWarnings("unchecked")
    private PatientOverviewDTO fetchPatientOverview() {
        try {
            Map<String, Object> stats = webClient.get()
                    .uri(patientServiceUrl + "/api/patients/statistics")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (stats == null) {
                return emptyPatientOverview();
            }

            return PatientOverviewDTO.builder()
                    .totalAdmitted(toInt(stats.get("totalAdmitted")))
                    .inTransfer(toInt(stats.get("inTransfer")))
                    .dischargedToday(toInt(stats.get("dischargedToday")))
                    .emergencies(toInt(stats.get("emergencies")))
                    .wardDistribution((Map<String, Object>) stats.get("wardDistribution"))
                    .build();
        } catch (Exception e) {
            log.error("Error fetching patient overview: {}", e.getMessage());
            return emptyPatientOverview();
        }
    }

    private Object fetchCapacityOverview() {
        try {
            return webClient.get()
                    .uri(capacityServiceUrl + "/api/capacity/current")
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
        } catch (Exception e) {
            log.error("Error fetching capacity overview: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Object> fetchRecentNotifications() {
        try {
            return webClient.get()
                    .uri(notificationServiceUrl + "/api/notifications?status=PENDING")
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();
        } catch (Exception e) {
            log.error("Error fetching notifications: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<String> generateAlerts(BedOverviewDTO bedOverview, PatientOverviewDTO patientOverview) {
        List<String> alerts = new ArrayList<>();

        if (bedOverview.getOccupancyRate() > 90) {
            alerts.add("CRITICAL: Hospital occupancy rate is above 90%!");
        } else if (bedOverview.getOccupancyRate() > 75) {
            alerts.add("WARNING: Hospital occupancy rate is above 75%.");
        }

        if (bedOverview.getFreeBeds() < 5) {
            alerts.add("CRITICAL: Less than 5 beds available!");
        }

        if (patientOverview.getEmergencies() > 5) {
            alerts.add("WARNING: High number of emergency patients (" + patientOverview.getEmergencies() + ").");
        }

        if (bedOverview.getCleaningBeds() > 10) {
            alerts.add("WARNING: " + bedOverview.getCleaningBeds() + " beds awaiting cleaning.");
        }

        return alerts;
    }

    private BedOverviewDTO emptyBedOverview() {
        return BedOverviewDTO.builder()
                .totalBeds(0).freeBeds(0).occupiedBeds(0)
                .reservedBeds(0).cleaningBeds(0).occupancyRate(0.0)
                .wardBreakdown(Collections.emptyMap())
                .build();
    }

    private PatientOverviewDTO emptyPatientOverview() {
        return PatientOverviewDTO.builder()
                .totalAdmitted(0).inTransfer(0).dischargedToday(0)
                .emergencies(0).wardDistribution(Collections.emptyMap())
                .build();
    }

    private int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        try { return Integer.parseInt(value.toString()); } catch (Exception e) { return 0; }
    }

    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try { return Double.parseDouble(value.toString()); } catch (Exception e) { return 0.0; }
    }
}
