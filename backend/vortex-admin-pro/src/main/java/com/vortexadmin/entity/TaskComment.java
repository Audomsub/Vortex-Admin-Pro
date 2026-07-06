package com.vortexadmin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a user-authored comment posted on a {@link Task}, forming the
 * threaded discussion history for that task.
 */
@Entity
@Table(name = "task_comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The task this comment belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    /** The user who authored this comment. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Free-form text body of the comment; stored as TEXT to support long entries. */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String comment;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Sets {@code createdAt} to the current time before the first database insert.
     */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
