package com.hospital.capacity.repository;

import com.hospital.capacity.model.OccupancyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OccupancyRecordRepository extends JpaRepository<OccupancyRecord, Long> {

    List<OccupancyRecord> findByWard(String ward);

    List<OccupancyRecord> findByDate(LocalDate date);

    List<OccupancyRecord> findByWardAndDateBetween(String ward, LocalDate startDate, LocalDate endDate);

    Optional<OccupancyRecord> findTopByWardOrderByRecordedAtDesc(String ward);
}
