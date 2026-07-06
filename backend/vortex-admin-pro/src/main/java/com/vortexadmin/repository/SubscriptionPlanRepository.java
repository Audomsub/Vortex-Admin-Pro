package com.vortexadmin.repository;

import com.vortexadmin.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link SubscriptionPlan} entities, providing standard CRUD
 * operations and methods for name-based lookup and existence checks.
 */
@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    /**
     * Finds a subscription plan by its unique name.
     *
     * @param name the plan name to search for (e.g., "Pro", "Enterprise")
     * @return an {@link Optional} containing the matching {@link SubscriptionPlan},
     *         or empty if no plan with that name exists
     */
    Optional<SubscriptionPlan> findByName(String name);

    /**
     * Checks whether a subscription plan with the given name already exists.
     * Used during plan creation to enforce name uniqueness.
     *
     * @param name the plan name to check
     * @return {@code true} if a plan with that name exists, otherwise {@code false}
     */
    boolean existsByName(String name);
}
