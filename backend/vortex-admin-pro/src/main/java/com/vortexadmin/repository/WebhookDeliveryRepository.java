package com.vortexadmin.repository;

import com.vortexadmin.entity.WebhookDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link WebhookDelivery} entities, providing standard CRUD
 * operations and a method to retrieve recent delivery attempts for a specific webhook endpoint.
 */
@Repository
public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, Long> {

    /**
     * Returns the 20 most recent delivery attempts for the specified webhook endpoint, ordered
     * by delivery timestamp descending.  Used to display the delivery log for an endpoint in
     * the admin UI.
     *
     * @param endpointId the primary key of the parent webhook endpoint
     * @return a list of up to 20 most-recent delivery records for the given endpoint
     */
    List<WebhookDelivery> findTop20ByWebhookEndpointIdOrderByDeliveredAtDesc(Long endpointId);
}
