package com.vortexadmin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a unit of work that can be assigned to a {@link User} within a {@link Team},
 * tracking its lifecycle from creation through completion.
 */
@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Workflow state of the task: TODO, IN_PROGRESS, or DONE; defaults to TODO on creation. */
    @Column(length = 50)
    private String status; // TODO, IN_PROGRESS, DONE

    /** Importance level of the task: LOW, MEDIUM, or HIGH; defaults to MEDIUM on creation. */
    @Column(length = 50)
    private String priority; // LOW, MEDIUM, HIGH

    /** The user responsible for completing this task; {@code null} means unassigned. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    /** The team this task belongs to; {@code null} means the task is not team-scoped. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    /** Deadline for the task; {@code null} means no due date has been set. */
    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Sets audit timestamps and applies default values for {@code status} and {@code priority}
     * before the first database insert.
     */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (status == null) status = "TODO";
        if (priority == null) priority = "MEDIUM";
    }

    /**
     * Refreshes {@code updatedAt} to the current time before every database update.
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
