package com.vortexadmin.repository;

import com.vortexadmin.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findFirstByOrganizationIdAndStatusOrderByCreatedAtDesc(Long organizationId, String status);
    List<Subscription> findByOrganizationId(Long organizationId);
    long countByStatus(String status);
}
