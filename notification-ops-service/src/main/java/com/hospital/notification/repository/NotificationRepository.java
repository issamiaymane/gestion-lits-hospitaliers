package com.hospital.notification.repository;

import com.hospital.notification.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByTargetWard(String targetWard);

    List<Notification> findByStatus(NotificationStatus status);

    List<Notification> findByType(NotificationType type);

    List<Notification> findByTargetWardAndStatus(String targetWard, NotificationStatus status);

    List<Notification> findByPriority(Priority priority);

    long countByStatus(NotificationStatus status);

    List<Notification> findByCreatedAtAfter(LocalDateTime dateTime);
}
