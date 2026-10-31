package com.brightloop.subscription.notification;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;import com.brightloop.subscription.common.ApiException;
import com.brightloop.subscription.common.PageResponse;
import com.brightloop.subscription.notification.dto.NotificationResponse;
import com.brightloop.subscription.security.CurrentUserService;
import com.brightloop.subscription.user.User;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final CurrentUserService currentUserService;

    public NotificationService(NotificationRepository notificationRepository,
            CurrentUserService currentUserService) {
        this.notificationRepository = notificationRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public void notify(User user, String type, String title, String message,
            String referenceType, Long referenceId) {
        notificationRepository.save(Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .build());
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> list(Pageable pageable) {
        User current = currentUserService.get();
        Page<Notification> page = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(current.getId(), pageable);
        return PageResponse.from(page, NotificationResponse::from);
    }

    @Transactional(readOnly = true)
    public long unreadCount() {
        return notificationRepository.countByUserIdAndReadFalse(currentUserService.get().getId());
    }

    @Transactional
    public NotificationResponse markRead(Long id) {
        User current = currentUserService.get();
        Notification notification = notificationRepository.findByIdAndUserId(id, current.getId())
                .orElseThrow(() -> ApiException.notFound("Notification"));
        notification.setRead(true);
        return NotificationResponse.from(notification);
    }

    @Transactional
    public int markAllRead() {
        User current = currentUserService.get();
        List<Notification> unread = notificationRepository.findByUserIdAndReadFalse(current.getId());
        unread.forEach(notification -> notification.setRead(true));
        return unread.size();
    }
}
