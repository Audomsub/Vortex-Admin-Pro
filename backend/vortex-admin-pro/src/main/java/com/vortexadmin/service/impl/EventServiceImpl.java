package com.vortexadmin.service.impl;

import com.vortexadmin.dto.request.EventRequest;
import com.vortexadmin.dto.response.EventResponse;
import com.vortexadmin.entity.Event;
import com.vortexadmin.entity.User;
import com.vortexadmin.exception.ApiException;
import com.vortexadmin.repository.EventRepository;
import com.vortexadmin.repository.UserRepository;
import com.vortexadmin.service.EventService;
import com.vortexadmin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    private EventResponse mapToResponse(Event event) {
        List<EventResponse.AttendeeInfo> attendeeInfos = event.getAttendees() == null
                ? Collections.emptyList()
                : event.getAttendees().stream()
                        .map(u -> EventResponse.AttendeeInfo.builder()
                                .id(u.getId())
                                .username(u.getUsername())
                                .firstName(u.getFirstName())
                                .lastName(u.getLastName())
                                .avatarUrl(u.getAvatarUrl())
                                .email(u.getEmail())
                                .build())
                        .collect(Collectors.toList());

        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .location(event.getLocation())
                .createdByUsername(event.getCreatedBy() != null ? event.getCreatedBy().getUsername() : null)
                .createdAt(event.getCreatedAt())
                .attendees(attendeeInfos)
                .build();
    }

    private Set<User> resolveAttendees(List<Long> attendeeIds) {
        if (attendeeIds == null || attendeeIds.isEmpty()) return new HashSet<>();
        return new HashSet<>(userRepository.findAllById(attendeeIds));
    }

    @Override
    public List<EventResponse> getAllEvents() {
        Long userId = SecurityUtils.getCurrentUserId();
        return eventRepository.findByCreatedByIdOrAttendeeId(userId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public EventResponse getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Event not found"));
        return mapToResponse(event);
    }

    @Override
    @Transactional
    public EventResponse createEvent(EventRequest request) {
        User creator = userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Start date and end date are required");
        }
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "End date cannot be before start date");
        }

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .location(request.getLocation())
                .createdBy(creator)
                .attendees(resolveAttendees(request.getAttendeeIds()))
                .build();

        return mapToResponse(eventRepository.save(event));
    }

    @Override
    @Transactional
    public void updateEvent(Long id, EventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Event not found"));

        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Start date and end date are required");
        }
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "End date cannot be before start date");
        }

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setLocation(request.getLocation());
        event.setAttendees(resolveAttendees(request.getAttendeeIds()));

        eventRepository.save(event);
    }

    @Override
    @Transactional
    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Event not found"));
        eventRepository.delete(event);
    }
}

