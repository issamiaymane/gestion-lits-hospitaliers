package com.hospital.patient.repository;

import com.hospital.patient.model.PatientTransfer;
import com.hospital.patient.model.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientTransferRepository extends JpaRepository<PatientTransfer, Long> {

    List<PatientTransfer> findByPatientId(Long patientId);

    List<PatientTransfer> findByStatus(TransferStatus status);

    List<PatientTransfer> findByToWard(String toWard);

    List<PatientTransfer> findByPatientIdAndStatus(Long patientId, TransferStatus status);
}
