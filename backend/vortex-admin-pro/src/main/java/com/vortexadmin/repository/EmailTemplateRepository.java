package com.vortexadmin.repository;

import com.vortexadmin.entity.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link EmailTemplate} entities, providing standard CRUD
 * operations and a name-based lookup method for retrieving email templates by their identifier.
 */
@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {

    /**
     * Finds an email template by its unique name.
     *
     * @param name the template name to search for (e.g., "welcome", "password-reset")
     * @return an {@link Optional} containing the matching {@link EmailTemplate},
     *         or empty if no template with that name exists
     */
    Optional<EmailTemplate> findByName(String name);
}
