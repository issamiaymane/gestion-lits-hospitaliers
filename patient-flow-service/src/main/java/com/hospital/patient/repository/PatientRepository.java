package com.hospital.patient.repository;

import com.hospital.patient.model.Patient;
import com.hospital.patient.model.PatientStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    List<Patient> findByCurrentStatus(PatientStatus status);

    List<Patient> findByCurrentWard(String ward);

    List<Patient> findByLastName(String lastName);

    Optional<Patient> findByNationalId(String nationalId);

    long countByCurrentStatus(PatientStatus status);

    long countByCurrentWard(String ward);
}
