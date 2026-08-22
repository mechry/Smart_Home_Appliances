package com.smart.home.dto;

import jakarta.validation.constraints.NotBlank;

public record RoomRequest(
        @NotBlank(message = "{room.name.required}") String name
) {
}
