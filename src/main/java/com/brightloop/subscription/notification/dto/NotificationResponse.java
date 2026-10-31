package com.brightloop.subscription.notification.dto;

import java.time.Instant;

import com.brightloop.subscription.notification.Notification;

public record NotificationResponse(
        Long id,
        String type,
        String title,
        String message,
        boolean read,
        String referenceType,
        Long referenceId,
        Instant createdAt) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.isRead(),
                notification.getReferenceType(),
                notification.getReferenceId(),
                notification.getCreatedAt());
    }
}
