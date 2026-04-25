package com.hospital.bed.repository;

import com.hospital.bed.model.Bed;
import com.hospital.bed.model.BedStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BedRepository extends JpaRepository<Bed, Long> {

    List<Bed> findByStatus(BedStatus status);

    List<Bed> findByWard(String ward);

    List<Bed> findByWardAndStatus(String ward, BedStatus status);

    Optional<Bed> findByBedNumber(String bedNumber);

    long countByStatus(BedStatus status);

    long countByWard(String ward);

    long countByWardAndStatus(String ward, BedStatus status);
}
