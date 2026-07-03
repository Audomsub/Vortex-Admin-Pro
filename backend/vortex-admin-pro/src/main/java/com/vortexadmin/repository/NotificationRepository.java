package com.vortexadmin.repository;

import com.vortexadmin.entity.Notification;
import com.vortexadmin.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    long countByUserAndIsReadFalse(User user);
    long countByUserAndCreatedAtBetween(User user, java.time.LocalDateTime start, java.time.LocalDateTime end);
}
