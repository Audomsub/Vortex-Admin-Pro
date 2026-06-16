package com.vortexadmin.service;

import com.vortexadmin.dto.request.EventRequest;
import com.vortexadmin.dto.response.EventResponse;

import java.util.List;

public interface EventService {
    List<EventResponse> getAllEvents();
    EventResponse getEventById(Long id);
    EventResponse createEvent(EventRequest request);
    void updateEvent(Long id, EventRequest request);
    void deleteEvent(Long id);
}
