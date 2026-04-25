package com.hospital.bed.controller;

import com.hospital.bed.dto.*;
import com.hospital.bed.model.BedAssignment;
import com.hospital.bed.model.BedStatus;
import com.hospital.bed.service.BedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beds")
@RequiredArgsConstructor
@Tag(name = "Bed Management")
public class BedController {

    private final BedService bedService;

    @GetMapping
    @Operation(summary = "List all beds with optional filters")
    public ResponseEntity<List<BedDTO>> getAllBeds(
            @RequestParam(required = false) String ward,
            @RequestParam(required = false) BedStatus status) {

        List<BedDTO> beds;
        if (ward != null && status != null) {
            beds = bedService.getBedsByWardAndStatus(ward, status);
        } else if (ward != null) {
            beds = bedService.getBedsByWard(ward);
        } else if (status != null) {
            beds = bedService.getBedsByStatus(status);
        } else {
            beds = bedService.getAllBeds();
        }
        return ResponseEntity.ok(beds);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bed by ID")
    public ResponseEntity<BedDTO> getBedById(@PathVariable Long id) {
        return ResponseEntity.ok(bedService.getBedById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new bed")
    public ResponseEntity<BedDTO> createBed(@RequestBody CreateBedRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bedService.createBed(request));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update bed status")
    public ResponseEntity<BedDTO> updateBedStatus(
            @PathVariable Long id,
            @RequestBody UpdateBedStatusRequest request) {
        return ResponseEntity.ok(bedService.updateBedStatus(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a bed")
    public ResponseEntity<Void> deleteBed(@PathVariable Long id) {
        bedService.deleteBed(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assign")
    @Operation(summary = "Assign a bed to a patient")
    public ResponseEntity<BedDTO> assignBed(@RequestBody AssignBedRequest request) {
        return ResponseEntity.ok(bedService.assignBed(request));
    }

    @PostMapping("/{id}/release")
    @Operation(summary = "Release a bed")
    public ResponseEntity<BedDTO> releaseBed(@PathVariable Long id) {
        return ResponseEntity.ok(bedService.releaseBed(id));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get bed statistics")
    public ResponseEntity<BedStatisticsDTO> getStatistics() {
        return ResponseEntity.ok(bedService.getStatistics());
    }

    @GetMapping("/assignments/{bedId}")
    @Operation(summary = "Get assignment history for a bed")
    public ResponseEntity<List<BedAssignment>> getBedAssignmentHistory(@PathVariable Long bedId) {
        return ResponseEntity.ok(bedService.getBedAssignmentHistory(bedId));
    }
}
