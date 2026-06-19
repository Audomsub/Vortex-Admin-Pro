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

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    @PreAuthorize("hasAuthority('calendar.read.own')")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getAllEvents() {
        return ResponseEntity.ok(ApiResponse.success("Events fetched", eventService.getAllEvents()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('calendar.read.own')")
    public ResponseEntity<ApiResponse<EventResponse>> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Event fetched", eventService.getEventById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('calendar.create') or hasAuthority('calendar.read.own')")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(@Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Event created", eventService.createEvent(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('calendar.update') or hasAuthority('calendar.read.own')")
    public ResponseEntity<ApiResponse<Void>> updateEvent(@PathVariable Long id, @Valid @RequestBody EventRequest request) {
        eventService.updateEvent(id, request);
        return ResponseEntity.ok(ApiResponse.success("Event updated", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('calendar.update') or hasAuthority('calendar.read.own')")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok(ApiResponse.success("Event deleted", null));
    }
}
