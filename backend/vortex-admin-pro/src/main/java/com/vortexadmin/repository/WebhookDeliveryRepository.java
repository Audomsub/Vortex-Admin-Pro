package com.vortexadmin.repository;

import com.vortexadmin.entity.WebhookDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, Long> {
    List<WebhookDelivery> findTop20ByWebhookEndpointIdOrderByDeliveredAtDesc(Long endpointId);
}
