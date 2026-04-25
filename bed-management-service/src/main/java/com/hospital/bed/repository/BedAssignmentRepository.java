package com.hospital.bed.repository;

import com.hospital.bed.model.BedAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BedAssignmentRepository extends JpaRepository<BedAssignment, Long> {

    Optional<BedAssignment> findByBedIdAndReleasedAtIsNull(Long bedId);

    List<BedAssignment> findByPatientId(Long patientId);

    List<BedAssignment> findByBedId(Long bedId);
}
