package com.vortexadmin.repository;

import com.vortexadmin.entity.WebhookEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebhookEndpointRepository extends JpaRepository<WebhookEndpoint, Long> {
    List<WebhookEndpoint> findAllByOrderByCreatedAtDesc();
    List<WebhookEndpoint> findByActiveTrue();
}
