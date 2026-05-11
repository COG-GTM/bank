package org.mounanga.notificationservice.dto;

public record CustomerResponseDTO(
        String id,
        String firstname,
        String lastname,
        String email) {
}
