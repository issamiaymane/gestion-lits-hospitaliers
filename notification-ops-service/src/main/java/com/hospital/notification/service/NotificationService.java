package com.hospital.notification.service;

import com.hospital.notification.dto.*;
import com.hospital.notification.model.*;
import com.hospital.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationDTO createNotification(CreateNotificationRequest request) {
        Notification notification = Notification.builder()
                .type(NotificationType.valueOf(request.getType()))
                .title(request.getTitle())
                .message(request.getMessage())
                .targetWard(request.getTargetWard())
                .targetUserId(request.getTargetUserId())
                .priority(Priority.valueOf(request.getPriority()))
                .status(NotificationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .bedId(request.getBedId())
                .patientId(request.getPatientId())
                .build();

        Notification saved = notificationRepository.save(notification);
        return toDTO(saved);
    }

    public List<NotificationDTO> getAll(String ward, String status, String type) {
        List<Notification> notifications;

        if (ward != null && status != null) {
            notifications = notificationRepository.findByTargetWardAndStatus(
                    ward, NotificationStatus.valueOf(status));
        } else if (ward != null) {
            notifications = notificationRepository.findByTargetWard(ward);
        } else if (status != null) {
            notifications = notificationRepository.findByStatus(NotificationStatus.valueOf(status));
        } else if (type != null) {
            notifications = notificationRepository.findByType(NotificationType.valueOf(type));
        } else {
            notifications = notificationRepository.findAll();
        }

        return notifications.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public NotificationDTO getById(Long id) {
        return notificationRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
    }

    public NotificationDTO markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(LocalDateTime.now());
        return toDTO(notificationRepository.save(notification));
    }

    public NotificationDTO markAsSent(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
        notification.setStatus(NotificationStatus.SENT);
        return toDTO(notificationRepository.save(notification));
    }

    public List<NotificationDTO> getByWard(String ward) {
        return notificationRepository.findByTargetWard(ward).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationDTO> getUnread(String ward) {
        return notificationRepository.findByTargetWardAndStatus(ward, NotificationStatus.PENDING).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public NotificationStatsDTO getStats() {
        List<Notification> all = notificationRepository.findAll();

        Map<String, Long> byType = all.stream()
                .collect(Collectors.groupingBy(n -> n.getType().name(), Collectors.counting()));

        Map<String, Long> byPriority = all.stream()
                .collect(Collectors.groupingBy(n -> n.getPriority().name(), Collectors.counting()));

        return NotificationStatsDTO.builder()
                .total(all.size())
                .pending(notificationRepository.countByStatus(NotificationStatus.PENDING))
                .sent(notificationRepository.countByStatus(NotificationStatus.SENT))
                .read(notificationRepository.countByStatus(NotificationStatus.READ))
                .byType(byType)
                .byPriority(byPriority)
                .build();
    }

    public void archiveOld(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        List<Notification> oldNotifications = notificationRepository.findByCreatedAtAfter(cutoff);

        List<Notification> allNotifications = notificationRepository.findAll();
        List<Notification> toArchive = allNotifications.stream()
                .filter(n -> n.getCreatedAt().isBefore(cutoff))
                .filter(n -> n.getStatus() != NotificationStatus.ARCHIVED)
                .collect(Collectors.toList());

        toArchive.forEach(n -> n.setStatus(NotificationStatus.ARCHIVED));
        notificationRepository.saveAll(toArchive);
        log.info("Archived {} old notifications older than {} days", toArchive.size(), days);
    }

    public NotificationDTO notifyBedRelease(Long bedId, String ward) {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .type(NotificationType.BED_RELEASE.name())
                .title("Bed Released")
                .message("Bed #" + bedId + " has been released in ward " + ward)
                .targetWard(ward)
                .priority(Priority.MEDIUM.name())
                .bedId(bedId)
                .build();
        return createNotification(request);
    }

    public NotificationDTO notifyCleaning(Long bedId, String ward) {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .type(NotificationType.CLEANING_REQUIRED.name())
                .title("Cleaning Required")
                .message("Bed #" + bedId + " in ward " + ward + " requires cleaning")
                .targetWard(ward)
                .priority(Priority.HIGH.name())
                .bedId(bedId)
                .build();
        return createNotification(request);
    }

    public NotificationDTO notifyTransfer(Long patientId, String fromWard, String toWard) {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .type(NotificationType.TRANSFER_ALERT.name())
                .title("Patient Transfer Alert")
                .message("Patient #" + patientId + " is being transferred from " + fromWard + " to " + toWard)
                .targetWard(toWard)
                .priority(Priority.HIGH.name())
                .patientId(patientId)
                .build();
        return createNotification(request);
    }

    public NotificationDTO notifyCapacityWarning(String ward, double occupancyRate) {
        Priority priority = occupancyRate > 90 ? Priority.URGENT : Priority.HIGH;
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .type(NotificationType.CAPACITY_WARNING.name())
                .title("Capacity Warning")
                .message("Ward " + ward + " is at " + String.format("%.1f", occupancyRate) + "% capacity")
                .targetWard(ward)
                .priority(priority.name())
                .build();
        return createNotification(request);
    }

    public NotificationDTO notifyEmergency(String ward, String message) {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .type(NotificationType.EMERGENCY.name())
                .title("EMERGENCY")
                .message(message)
                .targetWard(ward)
                .priority(Priority.URGENT.name())
                .build();
        return createNotification(request);
    }

    private NotificationDTO toDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .type(notification.getType().name())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .targetWard(notification.getTargetWard())
                .targetUserId(notification.getTargetUserId())
                .priority(notification.getPriority().name())
                .status(notification.getStatus().name())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .bedId(notification.getBedId())
                .patientId(notification.getPatientId())
                .build();
    }
}
