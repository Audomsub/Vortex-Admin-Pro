package com.vortexadmin.repository;

import com.vortexadmin.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link File} entities, providing standard CRUD operations
 * and a custom aggregate query to compute total storage usage across a set of users.
 */
@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    /**
     * Returns all files uploaded by the specified user.
     *
     * @param userId the primary key of the owning user
     * @return a list of files belonging to the given user
     */
    List<File> findByUserId(Long userId);

    /**
     * Calculates the total size (in bytes) of all files owned by users in the provided list.
     * Returns {@code 0} when no matching files exist (via COALESCE).
     * Used to compute aggregate storage consumption for an organisation or tenant.
     *
     * @param userIds the list of user IDs whose file sizes should be summed
     * @return the total number of bytes stored across all files belonging to the given users;
     *         {@code 0} if no files exist for those users
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM File f WHERE f.user.id IN :userIds")
    Long sumFileSizeByUserIds(@Param("userIds") List<Long> userIds);
}
