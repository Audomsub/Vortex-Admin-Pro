package com.vortexadmin.repository;

import com.vortexadmin.entity.WebhookEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link WebhookEndpoint} entities, providing standard CRUD
 * operations and custom methods for ordered retrieval and filtering by active status.
 */
@Repository
public interface WebhookEndpointRepository extends JpaRepository<WebhookEndpoint, Long> {

    /**
     * Returns all webhook endpoints ordered by creation date descending so the most recently
     * registered endpoints appear first.
     *
     * @return a list of all webhook endpoints, newest first
     */
    List<WebhookEndpoint> findAllByOrderByCreatedAtDesc();

    /**
     * Returns only the webhook endpoints that are currently active (enabled for event delivery).
     * Used when dispatching events to avoid sending payloads to disabled endpoints.
     *
     * @return a list of active webhook endpoints
     */
    List<WebhookEndpoint> findByActiveTrue();
}
