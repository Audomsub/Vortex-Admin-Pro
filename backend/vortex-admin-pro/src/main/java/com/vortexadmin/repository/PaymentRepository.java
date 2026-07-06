package com.vortexadmin.repository;

import com.vortexadmin.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Payment} entities, providing standard CRUD operations
 * and a method for retrieving all payments associated with a specific invoice.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Returns all payments recorded against the specified invoice.
     *
     * @param invoiceId the primary key of the parent invoice
     * @return a list of payments linked to the given invoice
     */
    List<Payment> findByInvoiceId(Long invoiceId);
}
