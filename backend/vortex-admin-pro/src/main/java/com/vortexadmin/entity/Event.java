package com.vortexadmin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a calendar event with a defined time window, an optional physical
 * or virtual location, and a set of invited {@link User} attendees.
 */
@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Date and time when the event begins. */
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    /** Date and time when the event ends. */
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    /** Optional physical address or virtual meeting URL for the event. */
    @Column
    private String location;

    /** The user who created the event. */
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    /** Set of users invited to or registered for this event, managed via the {@code event_attendees} join table. */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "event_attendees",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> attendees = new HashSet<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Automatically maintained by Hibernate on every update. */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Sets {@code createdAt} to the current time before the first database insert.
     */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
