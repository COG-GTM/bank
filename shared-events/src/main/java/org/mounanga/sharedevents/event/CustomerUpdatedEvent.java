package org.mounanga.sharedevents.event;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CustomerUpdatedEvent extends BaseEvent<String> {
    private final String firstname;
    private final String lastname;
    private final String email;

    public CustomerUpdatedEvent(String id, LocalDateTime eventDate, String eventBy, String firstname, String lastname, String email) {
        super(id, eventDate, eventBy);
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
    }
}
