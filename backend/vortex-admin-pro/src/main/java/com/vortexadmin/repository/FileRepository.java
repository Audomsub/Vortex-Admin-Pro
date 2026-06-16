package com.vortexadmin.repository;

import com.vortexadmin.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM File f WHERE f.user.id IN :userIds")
    Long sumFileSizeByUserIds(@Param("userIds") List<Long> userIds);
}
