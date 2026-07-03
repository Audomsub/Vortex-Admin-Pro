package com.vortexadmin.repository;

import com.vortexadmin.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    org.springframework.data.domain.Page<AuditLog> findAllByOrderByCreatedAtDesc(org.springframework.data.domain.Pageable pageable);
    List<AuditLog> findTop5ByOrderByCreatedAtDesc();
    List<AuditLog> findTop100ByUserIdOrderByCreatedAtDesc(Long userId);
    List<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);
}
