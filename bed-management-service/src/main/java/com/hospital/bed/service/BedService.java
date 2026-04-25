package com.hospital.bed.service;

import com.hospital.bed.dto.*;
import com.hospital.bed.model.*;
import com.hospital.bed.repository.BedAssignmentRepository;
import com.hospital.bed.repository.BedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BedService {

    private final BedRepository bedRepository;
    private final BedAssignmentRepository bedAssignmentRepository;

    public List<BedDTO> getAllBeds() {
        return bedRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public BedDTO getBedById(Long id) {
        Bed bed = bedRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bed not found with id: " + id));
        return toDTO(bed);
    }

    @Transactional
    public BedDTO createBed(CreateBedRequest request) {
        bedRepository.findByBedNumber(request.getBedNumber()).ifPresent(b -> {
            throw new RuntimeException("Bed with number " + request.getBedNumber() + " already exists");
        });

        Bed bed = Bed.builder()
                .bedNumber(request.getBedNumber())
                .ward(request.getWard())
                .roomNumber(request.getRoomNumber())
                .floor(request.getFloor())
                .bedType(request.getBedType())
                .status(BedStatus.FREE)
                .notes(request.getNotes())
                .build();

        return toDTO(bedRepository.save(bed));
    }

    @Transactional
    public BedDTO updateBedStatus(Long id, UpdateBedStatusRequest request) {
        Bed bed = bedRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bed not found with id: " + id));

        validateStatusTransition(bed.getStatus(), request.getStatus());

        bed.setStatus(request.getStatus());
        bed.setCurrentPatientId(request.getPatientId());
        if (request.getNotes() != null) {
            bed.setNotes(request.getNotes());
        }

        return toDTO(bedRepository.save(bed));
    }

    @Transactional
    public void deleteBed(Long id) {
        Bed bed = bedRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bed not found with id: " + id));
        if (bed.getStatus() == BedStatus.OCCUPIED) {
            throw new RuntimeException("Cannot delete an occupied bed. Release the patient first.");
        }
        bedRepository.delete(bed);
    }

    public List<BedDTO> getBedsByWard(String ward) {
        return bedRepository.findByWard(ward).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<BedDTO> getFreeBeds() {
        return bedRepository.findByStatus(BedStatus.FREE).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<BedDTO> getFreeBedsByWard(String ward) {
        return bedRepository.findByWardAndStatus(ward, BedStatus.FREE).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public BedDTO assignBed(AssignBedRequest request) {
        Bed bed = bedRepository.findById(request.getBedId())
                .orElseThrow(() -> new RuntimeException("Bed not found with id: " + request.getBedId()));

        if (bed.getStatus() != BedStatus.FREE && bed.getStatus() != BedStatus.RESERVED) {
            throw new RuntimeException("Bed is not available for assignment. Current status: " + bed.getStatus());
        }

        bed.setStatus(BedStatus.OCCUPIED);
        bed.setCurrentPatientId(request.getPatientId());
        bedRepository.save(bed);

        BedAssignment assignment = BedAssignment.builder()
                .bedId(request.getBedId())
                .patientId(request.getPatientId())
                .assignedAt(LocalDateTime.now())
                .assignedBy(request.getAssignedBy())
                .reason(request.getReason())
                .build();

        bedAssignmentRepository.save(assignment);

        return toDTO(bed);
    }

    @Transactional
    public BedDTO releaseBed(Long bedId) {
        Bed bed = bedRepository.findById(bedId)
                .orElseThrow(() -> new RuntimeException("Bed not found with id: " + bedId));

        if (bed.getStatus() != BedStatus.OCCUPIED) {
            throw new RuntimeException("Bed is not occupied. Current status: " + bed.getStatus());
        }

        bed.setStatus(BedStatus.CLEANING);
        bed.setCurrentPatientId(null);
        bedRepository.save(bed);

        bedAssignmentRepository.findByBedIdAndReleasedAtIsNull(bedId).ifPresent(assignment -> {
            assignment.setReleasedAt(LocalDateTime.now());
            bedAssignmentRepository.save(assignment);
        });

        return toDTO(bed);
    }

    public BedStatisticsDTO getStatistics() {
        long totalBeds = bedRepository.count();
        long freeBeds = bedRepository.countByStatus(BedStatus.FREE);
        long occupiedBeds = bedRepository.countByStatus(BedStatus.OCCUPIED);
        long reservedBeds = bedRepository.countByStatus(BedStatus.RESERVED);
        long cleaningBeds = bedRepository.countByStatus(BedStatus.CLEANING);
        double occupancyRate = totalBeds > 0 ? (double) occupiedBeds / totalBeds * 100 : 0.0;

        List<Bed> allBeds = bedRepository.findAll();
        Map<String, List<Bed>> bedsByWard = allBeds.stream()
                .filter(b -> b.getWard() != null)
                .collect(Collectors.groupingBy(Bed::getWard));

        Map<String, WardStats> statsByWard = new HashMap<>();
        for (Map.Entry<String, List<Bed>> entry : bedsByWard.entrySet()) {
            String ward = entry.getKey();
            List<Bed> wardBeds = entry.getValue();
            long wardTotal = wardBeds.size();
            long wardFree = wardBeds.stream().filter(b -> b.getStatus() == BedStatus.FREE).count();
            long wardOccupied = wardBeds.stream().filter(b -> b.getStatus() == BedStatus.OCCUPIED).count();
            long wardReserved = wardBeds.stream().filter(b -> b.getStatus() == BedStatus.RESERVED).count();
            long wardCleaning = wardBeds.stream().filter(b -> b.getStatus() == BedStatus.CLEANING).count();
            double wardOccupancyRate = wardTotal > 0 ? (double) wardOccupied / wardTotal * 100 : 0.0;

            statsByWard.put(ward, WardStats.builder()
                    .ward(ward)
                    .total(wardTotal)
                    .free(wardFree)
                    .occupied(wardOccupied)
                    .reserved(wardReserved)
                    .cleaning(wardCleaning)
                    .occupancyRate(wardOccupancyRate)
                    .build());
        }

        return BedStatisticsDTO.builder()
                .totalBeds(totalBeds)
                .freeBeds(freeBeds)
                .occupiedBeds(occupiedBeds)
                .reservedBeds(reservedBeds)
                .cleaningBeds(cleaningBeds)
                .occupancyRate(occupancyRate)
                .statsByWard(statsByWard)
                .build();
    }

    public List<BedAssignment> getBedAssignmentHistory(Long bedId) {
        return bedAssignmentRepository.findByBedId(bedId);
    }

    public List<BedDTO> getBedsByWardAndStatus(String ward, BedStatus status) {
        return bedRepository.findByWardAndStatus(ward, status).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<BedDTO> getBedsByStatus(BedStatus status) {
        return bedRepository.findByStatus(status).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private void validateStatusTransition(BedStatus currentStatus, BedStatus newStatus) {
        if (currentStatus == newStatus) {
            return;
        }

        boolean valid = switch (currentStatus) {
            case FREE -> newStatus == BedStatus.OCCUPIED || newStatus == BedStatus.RESERVED;
            case OCCUPIED -> newStatus == BedStatus.CLEANING;
            case CLEANING -> newStatus == BedStatus.FREE;
            case RESERVED -> newStatus == BedStatus.OCCUPIED || newStatus == BedStatus.FREE;
        };

        if (!valid) {
            throw new RuntimeException(
                    "Invalid status transition from " + currentStatus + " to " + newStatus +
                    ". Allowed transitions: FREE -> OCCUPIED/RESERVED, OCCUPIED -> CLEANING, " +
                    "CLEANING -> FREE, RESERVED -> OCCUPIED/FREE");
        }
    }

    private BedDTO toDTO(Bed bed) {
        return BedDTO.builder()
                .id(bed.getId())
                .bedNumber(bed.getBedNumber())
                .ward(bed.getWard())
                .roomNumber(bed.getRoomNumber())
                .floor(bed.getFloor())
                .bedType(bed.getBedType())
                .status(bed.getStatus())
                .currentPatientId(bed.getCurrentPatientId())
                .lastStatusChange(bed.getLastStatusChange())
                .notes(bed.getNotes())
                .build();
    }
}
