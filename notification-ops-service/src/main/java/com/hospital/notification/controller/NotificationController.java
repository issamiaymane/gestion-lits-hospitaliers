package com.hospital.notification.controller;

import com.hospital.notification.dto.*;
import com.hospital.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get all notifications with optional filters")
    public ResponseEntity<List<NotificationDTO>> getAll(
            @RequestParam(required = false) String ward,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(notificationService.getAll(ward, status, type));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID")
    public ResponseEntity<NotificationDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new notification")
    public ResponseEntity<NotificationDTO> create(@RequestBody CreateNotificationRequest request) {
        return ResponseEntity.ok(notificationService.createNotification(request));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<NotificationDTO> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PutMapping("/{id}/sent")
    @Operation(summary = "Mark notification as sent")
    public ResponseEntity<NotificationDTO> markAsSent(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsSent(id));
    }

    @GetMapping("/ward/{ward}/unread")
    @Operation(summary = "Get unread notifications for a ward")
    public ResponseEntity<List<NotificationDTO>> getUnread(@PathVariable String ward) {
        return ResponseEntity.ok(notificationService.getUnread(ward));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get notification statistics")
    public ResponseEntity<NotificationStatsDTO> getStats() {
        return ResponseEntity.ok(notificationService.getStats());
    }

    @PostMapping("/bed-release")
    @Operation(summary = "Quick bed release notification")
    public ResponseEntity<NotificationDTO> bedRelease(@RequestBody Map<String, Object> request) {
        Long bedId = Long.valueOf(request.get("bedId").toString());
        String ward = (String) request.get("ward");
        return ResponseEntity.ok(notificationService.notifyBedRelease(bedId, ward));
    }

    @PostMapping("/cleaning")
    @Operation(summary = "Quick cleaning notification")
    public ResponseEntity<NotificationDTO> cleaning(@RequestBody Map<String, Object> request) {
        Long bedId = Long.valueOf(request.get("bedId").toString());
        String ward = (String) request.get("ward");
        return ResponseEntity.ok(notificationService.notifyCleaning(bedId, ward));
    }

    @PostMapping("/transfer-alert")
    @Operation(summary = "Quick transfer alert notification")
    public ResponseEntity<NotificationDTO> transferAlert(@RequestBody Map<String, Object> request) {
        Long patientId = Long.valueOf(request.get("patientId").toString());
        String fromWard = (String) request.get("fromWard");
        String toWard = (String) request.get("toWard");
        return ResponseEntity.ok(notificationService.notifyTransfer(patientId, fromWard, toWard));
    }
}
