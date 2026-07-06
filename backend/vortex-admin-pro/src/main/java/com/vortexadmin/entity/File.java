package com.vortexadmin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a file uploaded by a {@link User} and stored in the system,
 * retaining metadata such as MIME type and size alongside the retrieval URL.
 */
@Entity
@Table(name = "files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Original filename as provided by the uploader, preserved for display and download. */
    @Column(name = "file_name", nullable = false)
    private String fileName;

    /** Publicly accessible or pre-signed URL pointing to the stored file object. */
    @Column(name = "file_url", nullable = false, columnDefinition = "TEXT")
    private String fileUrl;

    /** MIME type of the uploaded file (e.g., "image/png", "application/pdf"). */
    @Column(name = "file_type")
    private String fileType; // image/png, application/pdf, etc.

    /** Size of the stored file in bytes. */
    @Column(name = "file_size")
    private Long fileSize; // bytes

    /** The user who uploaded this file. */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    /**
     * Records {@code uploadedAt} as the current time before the first database insert.
     */
    @PrePersist
    public void prePersist() {
        uploadedAt = LocalDateTime.now();
    }
}
