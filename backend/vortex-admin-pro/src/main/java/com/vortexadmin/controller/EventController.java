package com.vortexadmin.controller;

import com.vortexadmin.dto.request.EventRequest;
import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.dto.response.EventResponse;
import com.vortexadmin.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Handles HTTP requests for calendar event management, supporting full CRUD operations
 * on user-owned events, delegating all business logic to EventService.
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /**
     * Retrieves all calendar events belonging to the authenticated user.
     *
     * @return a list of {@link EventResponse} objects representing the user's events
     */
    @GetMapping
    @PreAuthorize("hasAuthority('calendar.read.own')")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getAllEvents() {
        return ResponseEntity.ok(ApiResponse.success("Events fetched", eventService.getAllEvents()));
    }

    /**
     * Retrieves a single calendar event by its unique identifier.
     *
     * @param id the unique ID of the event to retrieve
     * @return the {@link EventResponse} for the specified event
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('calendar.read.own')")
    public ResponseEntity<ApiResponse<EventResponse>> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Event fetched", eventService.getEventById(id)));
    }

    /**
     * Creates a new calendar event with the provided details.
     *
     * @param request the event creation payload containing title, start/end times, and optional description
     * @return the created {@link EventResponse} reflecting the persisted event
     */
    @PostMapping
    @PreAuthorize("hasAuthority('calendar.create') or hasAuthority('calendar.read.own')")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(@Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Event created", eventService.createEvent(request)));
    }

    /**
     * Updates an existing calendar event by its unique identifier.
     *
     * @param id      the unique ID of the event to update
     * @param request the update payload containing the new event details
     * @return a success response with no data payload upon successful update
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('calendar.update') or hasAuthority('calendar.read.own')")
    public ResponseEntity<ApiResponse<Void>> updateEvent(@PathVariable Long id, @Valid @RequestBody EventRequest request) {
        eventService.updateEvent(id, request);
        return ResponseEntity.ok(ApiResponse.success("Event updated", null));
    }

    /**
     * Deletes a calendar event by its unique identifier.
     *
     * @param id the unique ID of the event to delete
     * @return a success response with no data payload upon successful deletion
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('calendar.update') or hasAuthority('calendar.read.own')")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok(ApiResponse.success("Event deleted", null));
    }
}
