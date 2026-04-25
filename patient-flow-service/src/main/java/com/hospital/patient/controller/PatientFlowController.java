package com.hospital.patient.controller;

import com.hospital.patient.dto.*;
import com.hospital.patient.model.PatientStatus;
import com.hospital.patient.model.PatientTransfer;
import com.hospital.patient.service.PatientFlowService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@Tag(name = "Patient Flow")
@RequiredArgsConstructor
public class PatientFlowController {

    private final PatientFlowService patientFlowService;

    @GetMapping
    public ResponseEntity<List<PatientDTO>> getPatients(
            @RequestParam(required = false) String ward,
            @RequestParam(required = false) PatientStatus status) {
        if (ward != null) {
            return ResponseEntity.ok(patientFlowService.getPatientsByWard(ward));
        }
        if (status != null) {
            return ResponseEntity.ok(patientFlowService.getPatientsByStatus(status));
        }
        return ResponseEntity.ok(patientFlowService.getAllPatients());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientDTO> getPatientById(@PathVariable Long id) {
        return ResponseEntity.ok(patientFlowService.getPatientById(id));
    }

    @PostMapping
    public ResponseEntity<PatientDTO> createPatient(@RequestBody CreatePatientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientFlowService.createPatient(request));
    }

    @PostMapping("/{id}/admit")
    public ResponseEntity<PatientDTO> admitPatient(@PathVariable Long id, @RequestBody AdmitPatientRequest request) {
        return ResponseEntity.ok(patientFlowService.admitPatient(id, request));
    }

    @PostMapping("/{id}/transfer")
    public ResponseEntity<PatientDTO> transferPatient(@PathVariable Long id, @RequestBody TransferPatientRequest request) {
        return ResponseEntity.ok(patientFlowService.transferPatient(id, request));
    }

    @PostMapping("/transfers/{transferId}/complete")
    public ResponseEntity<PatientDTO> completeTransfer(@PathVariable Long transferId) {
        return ResponseEntity.ok(patientFlowService.completeTransfer(transferId));
    }

    @PostMapping("/{id}/discharge")
    public ResponseEntity<PatientDTO> dischargePatient(@PathVariable Long id, @RequestBody DischargePatientRequest request) {
        return ResponseEntity.ok(patientFlowService.dischargePatient(id, request));
    }

    @GetMapping("/{id}/transfers")
    public ResponseEntity<List<PatientTransfer>> getTransferHistory(@PathVariable Long id) {
        return ResponseEntity.ok(patientFlowService.getTransferHistory(id));
    }

    @GetMapping("/statistics")
    public ResponseEntity<PatientFlowStatisticsDTO> getStatistics() {
        return ResponseEntity.ok(patientFlowService.getStatistics());
    }
}
