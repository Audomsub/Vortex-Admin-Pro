package com.vortexadmin.service;

import com.vortexadmin.dto.request.EventRequest;
import com.vortexadmin.dto.response.EventResponse;

import java.util.List;

/**
 * Service contract for calendar event management, providing full CRUD operations for events
 * associated with the currently authenticated user or their organization.
 */
public interface EventService {

    /**
     * Returns all events visible to the currently authenticated user (events they created or
     * are listed as an attendee of), ordered by start date ascending.
     *
     * @return a list of event responses relevant to the calling user
     */
    List<EventResponse> getAllEvents();

    /**
     * Returns a single event by its primary key.
     *
     * @param id the primary key of the event to retrieve
     * @return the matching event response
     * @throws com.vortexadmin.exception.ApiException if no event with the given ID exists
     */
    EventResponse getEventById(Long id);

    /**
     * Creates a new event using the provided details and returns the persisted event response.
     *
     * @param request the event creation payload including title, description, start/end dates,
     *                and attendee list
     * @return the newly created event response
     */
    EventResponse createEvent(EventRequest request);

    /**
     * Updates an existing event with the provided data.
     *
     * @param id      the primary key of the event to update
     * @param request the updated event data
     * @throws com.vortexadmin.exception.ApiException if the event is not found
     */
    void updateEvent(Long id, EventRequest request);

    /**
     * Deletes the specified event.
     *
     * @param id the primary key of the event to delete
     * @throws com.vortexadmin.exception.ApiException if the event is not found
     */
    void deleteEvent(Long id);
}
