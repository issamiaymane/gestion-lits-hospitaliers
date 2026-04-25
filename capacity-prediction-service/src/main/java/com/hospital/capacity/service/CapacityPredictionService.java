package com.hospital.capacity.service;

import com.hospital.capacity.dto.*;
import com.hospital.capacity.model.OccupancyRecord;
import com.hospital.capacity.repository.OccupancyRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CapacityPredictionService {

    private final OccupancyRecordRepository occupancyRecordRepository;
    private final WebClient webClient;

    @Value("${services.bed-management-service.url}")
    private String bedManagementServiceUrl;

    @Value("${services.patient-flow-service.url}")
    private String patientFlowServiceUrl;

    public CapacityPredictionService(OccupancyRecordRepository occupancyRecordRepository,
                                     WebClient.Builder webClientBuilder) {
        this.occupancyRecordRepository = occupancyRecordRepository;
        this.webClient = webClientBuilder.build();
    }

    @SuppressWarnings("unchecked")
    public OverallCapacityDTO getCurrentCapacity() {
        try {
            Map<String, Object> stats = webClient.get()
                    .uri(bedManagementServiceUrl + "/api/beds/statistics")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (stats == null) {
                return buildEmptyCapacity();
            }

            int totalBeds = (int) stats.getOrDefault("totalBeds", 0);
            int occupiedBeds = (int) stats.getOrDefault("occupiedBeds", 0);
            int freeBeds = (int) stats.getOrDefault("freeBeds", 0);
            double occupancyRate = totalBeds > 0 ? (double) occupiedBeds / totalBeds * 100.0 : 0.0;

            List<WardCapacityDTO> wardCapacities = new ArrayList<>();
            Object wardStatsObj = stats.get("wardStatistics");
            if (wardStatsObj instanceof Map) {
                Map<String, Map<String, Object>> wardStats = (Map<String, Map<String, Object>>) wardStatsObj;
                for (Map.Entry<String, Map<String, Object>> entry : wardStats.entrySet()) {
                    Map<String, Object> ws = entry.getValue();
                    int wTotal = (int) ws.getOrDefault("totalBeds", 0);
                    int wOccupied = (int) ws.getOrDefault("occupiedBeds", 0);
                    int wFree = (int) ws.getOrDefault("freeBeds", 0);
                    double wRate = wTotal > 0 ? (double) wOccupied / wTotal * 100.0 : 0.0;
                    wardCapacities.add(WardCapacityDTO.builder()
                            .ward(entry.getKey())
                            .totalBeds(wTotal)
                            .occupiedBeds(wOccupied)
                            .freeBeds(wFree)
                            .occupancyRate(wRate)
                            .build());
                }
            }

            List<CapacityPredictionDTO> predictions = wardCapacities.stream()
                    .map(wc -> getPrediction(wc.getWard()))
                    .collect(Collectors.toList());

            return OverallCapacityDTO.builder()
                    .totalBeds(totalBeds)
                    .occupiedBeds(occupiedBeds)
                    .freeBeds(freeBeds)
                    .overallOccupancyRate(occupancyRate)
                    .wardCapacities(wardCapacities)
                    .predictions(predictions)
                    .build();

        } catch (Exception e) {
            log.error("Error fetching capacity from bed-management-service: {}", e.getMessage());
            return buildEmptyCapacity();
        }
    }

    public CapacityPredictionDTO getPrediction(String ward) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate twoDaysAgo = today.minusDays(2);

        List<OccupancyRecord> last24h = occupancyRecordRepository
                .findByWardAndDateBetween(ward, yesterday, today);
        List<OccupancyRecord> previous24h = occupancyRecordRepository
                .findByWardAndDateBetween(ward, twoDaysAgo, yesterday);

        double currentAvg = last24h.stream()
                .mapToDouble(OccupancyRecord::getOccupancyRate)
                .average()
                .orElse(0.0);

        double previousAvg = previous24h.stream()
                .mapToDouble(OccupancyRecord::getOccupancyRate)
                .average()
                .orElse(0.0);

        double trendDiff = currentAvg - previousAvg;
        String trend;
        if (trendDiff > 5.0) {
            trend = "INCREASING";
        } else if (trendDiff < -5.0) {
            trend = "DECREASING";
        } else {
            trend = "STABLE";
        }

        double predictedOccupancy = currentAvg + trendDiff;
        predictedOccupancy = Math.max(0, Math.min(100, predictedOccupancy));

        Optional<OccupancyRecord> latestRecord = occupancyRecordRepository
                .findTopByWardOrderByRecordedAtDesc(ward);
        int totalBeds = latestRecord.map(OccupancyRecord::getTotalBeds).orElse(0);
        int predictedAvailableBeds = totalBeds > 0
                ? (int) Math.round(totalBeds * (1 - predictedOccupancy / 100.0))
                : 0;

        String riskLevel;
        if (currentAvg > 90) {
            riskLevel = "CRITICAL";
        } else if (currentAvg > 75) {
            riskLevel = "HIGH";
        } else if (currentAvg > 50) {
            riskLevel = "MEDIUM";
        } else {
            riskLevel = "LOW";
        }

        List<String> recommendations = generateRecommendations(riskLevel, trend, ward);

        return CapacityPredictionDTO.builder()
                .ward(ward)
                .currentOccupancy(currentAvg)
                .predictedOccupancy(predictedOccupancy)
                .predictedAvailableBeds(predictedAvailableBeds)
                .trend(trend)
                .riskLevel(riskLevel)
                .recommendations(recommendations)
                .build();
    }

    public OccupancyRecord recordOccupancy(String ward, int totalBeds, int occupiedBeds) {
        OccupancyRecord record = OccupancyRecord.builder()
                .ward(ward)
                .date(LocalDate.now())
                .hour(LocalDateTime.now().getHour())
                .totalBeds(totalBeds)
                .occupiedBeds(occupiedBeds)
                .occupancyRate(totalBeds > 0 ? (double) occupiedBeds / totalBeds * 100.0 : 0.0)
                .recordedAt(LocalDateTime.now())
                .build();
        return occupancyRecordRepository.save(record);
    }

    public List<CapacityPredictionDTO> getAllPredictions() {
        List<String> wards = occupancyRecordRepository.findAll().stream()
                .map(OccupancyRecord::getWard)
                .distinct()
                .collect(Collectors.toList());

        if (wards.isEmpty()) {
            return Collections.emptyList();
        }

        return wards.stream()
                .map(this::getPrediction)
                .collect(Collectors.toList());
    }

    private List<String> generateRecommendations(String riskLevel, String trend, String ward) {
        List<String> recommendations = new ArrayList<>();

        switch (riskLevel) {
            case "CRITICAL":
                recommendations.add("URGENT: Ward " + ward + " is at critical capacity. Consider emergency overflow protocols.");
                recommendations.add("Expedite discharge processes for stable patients.");
                recommendations.add("Activate surge capacity plans.");
                recommendations.add("Consider diverting new admissions to other wards.");
                break;
            case "HIGH":
                recommendations.add("Ward " + ward + " is approaching critical capacity. Review discharge plans.");
                recommendations.add("Prepare overflow areas as a precaution.");
                recommendations.add("Increase bed turnover monitoring.");
                break;
            case "MEDIUM":
                recommendations.add("Ward " + ward + " is at moderate capacity. Monitor trends closely.");
                recommendations.add("Ensure cleaning teams are on standby for quick bed turnover.");
                break;
            case "LOW":
                recommendations.add("Ward " + ward + " has adequate capacity. Normal operations.");
                break;
        }

        if ("INCREASING".equals(trend)) {
            recommendations.add("Occupancy trend is increasing. Plan ahead for potential capacity constraints.");
        } else if ("DECREASING".equals(trend)) {
            recommendations.add("Occupancy trend is decreasing. Consider scheduled maintenance on freed beds.");
        }

        return recommendations;
    }

    private OverallCapacityDTO buildEmptyCapacity() {
        return OverallCapacityDTO.builder()
                .totalBeds(0)
                .occupiedBeds(0)
                .freeBeds(0)
                .overallOccupancyRate(0.0)
                .wardCapacities(Collections.emptyList())
                .predictions(Collections.emptyList())
                .build();
    }
}
