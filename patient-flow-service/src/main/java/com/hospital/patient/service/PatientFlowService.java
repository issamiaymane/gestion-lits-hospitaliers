package com.hospital.patient.service;

import com.hospital.patient.dto.*;
import com.hospital.patient.model.*;
import com.hospital.patient.repository.PatientRepository;
import com.hospital.patient.repository.PatientTransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientFlowService {

    private final PatientRepository patientRepository;
    private final PatientTransferRepository patientTransferRepository;

    public List<PatientDTO> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PatientDTO getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + id));
        return toDTO(patient);
    }

    @Transactional
    public PatientDTO createPatient(CreatePatientRequest request) {
        Patient patient = Patient.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .nationalId(request.getNationalId())
                .contactPhone(request.getContactPhone())
                .emergencyContact(request.getEmergencyContact())
                .currentStatus(PatientStatus.REGISTERED)
                .build();
        Patient saved = patientRepository.save(patient);
        return toDTO(saved);
    }

    @Transactional
    public PatientDTO admitPatient(Long id, AdmitPatientRequest request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + id));
        patient.setCurrentStatus(PatientStatus.ADMITTED);
        patient.setCurrentWard(request.getWard());
        patient.setAdmissionDate(LocalDateTime.now());
        patient.setMedicalNotes(request.getMedicalNotes());
        Patient saved = patientRepository.save(patient);
        return toDTO(saved);
    }

    @Transactional
    public PatientDTO transferPatient(Long id, TransferPatientRequest request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + id));

        PatientTransfer transfer = PatientTransfer.builder()
                .patientId(id)
                .fromWard(patient.getCurrentWard())
                .toWard(request.getToWard())
                .transferDate(LocalDateTime.now())
                .reason(request.getReason())
                .authorizedBy(request.getAuthorizedBy())
                .status(TransferStatus.PENDING)
                .build();
        patientTransferRepository.save(transfer);

        patient.setCurrentStatus(PatientStatus.IN_TRANSFER);
        Patient saved = patientRepository.save(patient);
        return toDTO(saved);
    }

    @Transactional
    public PatientDTO completeTransfer(Long transferId) {
        PatientTransfer transfer = patientTransferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer not found with id: " + transferId));
        transfer.setStatus(TransferStatus.COMPLETED);
        patientTransferRepository.save(transfer);

        Patient patient = patientRepository.findById(transfer.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + transfer.getPatientId()));
        patient.setCurrentWard(transfer.getToWard());
        patient.setCurrentStatus(PatientStatus.ADMITTED);
        Patient saved = patientRepository.save(patient);
        return toDTO(saved);
    }

    @Transactional
    public PatientDTO dischargePatient(Long id, DischargePatientRequest request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + id));
        patient.setCurrentStatus(PatientStatus.DISCHARGED);
        patient.setDischargeDate(LocalDateTime.now());
        if (request.getNotes() != null) {
            patient.setMedicalNotes(request.getNotes());
        }
        Patient saved = patientRepository.save(patient);
        return toDTO(saved);
    }

    public List<PatientDTO> getPatientsByWard(String ward) {
        return patientRepository.findByCurrentWard(ward).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<PatientDTO> getPatientsByStatus(PatientStatus status) {
        return patientRepository.findByCurrentStatus(status).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<PatientTransfer> getTransferHistory(Long patientId) {
        return patientTransferRepository.findByPatientId(patientId);
    }

    public PatientFlowStatisticsDTO getStatistics() {
        long totalPatients = patientRepository.count();
        long admitted = patientRepository.countByCurrentStatus(PatientStatus.ADMITTED);
        long inTransfer = patientRepository.countByCurrentStatus(PatientStatus.IN_TRANSFER);
        long discharged = patientRepository.countByCurrentStatus(PatientStatus.DISCHARGED);
        long emergency = patientRepository.countByCurrentStatus(PatientStatus.EMERGENCY);

        List<Patient> admittedPatients = patientRepository.findByCurrentStatus(PatientStatus.ADMITTED);
        Map<String, Long> admissionsByWard = admittedPatients.stream()
                .filter(p -> p.getCurrentWard() != null)
                .collect(Collectors.groupingBy(Patient::getCurrentWard, Collectors.counting()));

        List<Patient> dischargedPatients = patientRepository.findByCurrentStatus(PatientStatus.DISCHARGED);
        double averageStayDuration = dischargedPatients.stream()
                .filter(p -> p.getAdmissionDate() != null && p.getDischargeDate() != null)
                .mapToLong(p -> Duration.between(p.getAdmissionDate(), p.getDischargeDate()).toHours())
                .average()
                .orElse(0.0);

        return PatientFlowStatisticsDTO.builder()
                .totalPatients(totalPatients)
                .admitted(admitted)
                .inTransfer(inTransfer)
                .discharged(discharged)
                .emergency(emergency)
                .admissionsByWard(admissionsByWard)
                .averageStayDuration(averageStayDuration)
                .build();
    }

    private PatientDTO toDTO(Patient patient) {
        return PatientDTO.builder()
                .id(patient.getId())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .dateOfBirth(patient.getDateOfBirth())
                .gender(patient.getGender())
                .nationalId(patient.getNationalId())
                .contactPhone(patient.getContactPhone())
                .emergencyContact(patient.getEmergencyContact())
                .currentStatus(patient.getCurrentStatus())
                .currentWard(patient.getCurrentWard())
                .admissionDate(patient.getAdmissionDate())
                .dischargeDate(patient.getDischargeDate())
                .medicalNotes(patient.getMedicalNotes())
                .build();
    }
}
