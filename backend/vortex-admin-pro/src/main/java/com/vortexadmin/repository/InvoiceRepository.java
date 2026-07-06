package com.vortexadmin.repository;

import com.vortexadmin.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for {@link Invoice} entities, providing standard CRUD operations
 * and custom queries for organization-scoped invoice retrieval, revenue aggregation over time
 * windows, status-filtered date-range queries, and bulk deletion during organization teardown.
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    /**
     * Returns all invoices belonging to the specified organization by navigating through the
     * {@code subscription -> organization} relationship, ordered by issue date descending.
     *
     * @param organizationId the primary key of the organization
     * @return a list of invoices for the given organization, newest first
     */
    @Query("SELECT i FROM Invoice i WHERE i.subscription.organization.id = :organizationId ORDER BY i.issuedAt DESC")
    List<Invoice> findByOrganizationId(@Param("organizationId") Long organizationId);

    /**
     * Calculates the total paid revenue from invoices issued on or after the given date.
     * Only invoices with status {@code 'PAID'} are included.  Returns {@code 0} when no
     * matching invoices exist (via COALESCE).
     *
     * @param from the inclusive lower boundary for the {@code issuedAt} timestamp
     * @return the sum of amounts for all paid invoices issued since {@code from};
     *         {@code 0} if none exist
     */
    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Invoice i WHERE i.status = 'PAID' AND i.issuedAt >= :from")
    BigDecimal sumPaidAmountSince(@Param("from") LocalDateTime from);

    /**
     * Calculates the total paid revenue from invoices issued within the given time window.
     * Only invoices with status {@code 'PAID'} are included.  Returns {@code 0} when no
     * matching invoices exist (via COALESCE).
     *
     * @param from the inclusive start of the time range
     * @param to   the inclusive end of the time range
     * @return the sum of amounts for all paid invoices issued between {@code from} and {@code to};
     *         {@code 0} if none exist
     */
    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Invoice i WHERE i.status = 'PAID' AND i.issuedAt BETWEEN :from AND :to")
    BigDecimal sumPaidAmountBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /**
     * Returns all invoices matching the specified status whose {@code issuedAt} falls within the
     * given time window, ordered by issue date ascending.  Used for generating time-series
     * revenue charts over a reporting period.
     *
     * @param status the invoice status to filter by (e.g., "PAID", "PENDING")
     * @param start  the inclusive start of the time range
     * @param end    the inclusive end of the time range
     * @return a list of matching invoices in ascending issue-date order
     */
    List<Invoice> findByStatusAndIssuedAtBetweenOrderByIssuedAtAsc(String status, LocalDateTime start, LocalDateTime end);

    /**
     * Deletes all invoices whose parent subscription belongs to the specified organization,
     * using a native SQL DELETE that targets the {@code invoices} table directly.
     * Intended for use during organization hard-delete flows where cascading is handled manually.
     *
     * @param organizationId the primary key of the organization whose invoices should be removed
     */
    @Modifying
    @Query(value = "DELETE FROM invoices WHERE subscription_id IN (SELECT id FROM subscriptions WHERE organization_id = :organizationId)", nativeQuery = true)
    void deleteByOrganizationId(@Param("organizationId") Long organizationId);
}
