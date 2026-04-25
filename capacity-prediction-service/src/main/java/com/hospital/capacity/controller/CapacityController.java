package com.hospital.capacity.controller;

import com.hospital.capacity.dto.CapacityPredictionDTO;
import com.hospital.capacity.dto.OverallCapacityDTO;
import com.hospital.capacity.model.OccupancyRecord;
import com.hospital.capacity.service.CapacityPredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/capacity")
@RequiredArgsConstructor
@Tag(name = "Capacity Prediction")
public class CapacityController {

    private final CapacityPredictionService capacityPredictionService;

    @GetMapping("/current")
    @Operation(summary = "Get current overall capacity")
    public ResponseEntity<OverallCapacityDTO> getCurrentCapacity() {
        return ResponseEntity.ok(capacityPredictionService.getCurrentCapacity());
    }

    @GetMapping("/prediction/{ward}")
    @Operation(summary = "Get prediction for a specific ward")
    public ResponseEntity<CapacityPredictionDTO> getPrediction(@PathVariable String ward) {
        return ResponseEntity.ok(capacityPredictionService.getPrediction(ward));
    }

    @GetMapping("/predictions")
    @Operation(summary = "Get predictions for all wards")
    public ResponseEntity<List<CapacityPredictionDTO>> getAllPredictions() {
        return ResponseEntity.ok(capacityPredictionService.getAllPredictions());
    }

    @PostMapping("/record")
    @Operation(summary = "Record an occupancy snapshot")
    public ResponseEntity<OccupancyRecord> recordOccupancy(@RequestBody Map<String, Object> request) {
        String ward = (String) request.get("ward");
        int totalBeds = (int) request.get("totalBeds");
        int occupiedBeds = (int) request.get("occupiedBeds");
        return ResponseEntity.ok(capacityPredictionService.recordOccupancy(ward, totalBeds, occupiedBeds));
    }
}
