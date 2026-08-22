package com.smart.home.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FanRequest(
        @NotBlank(message = "{fan.name.required}") String name,
        @NotNull(message = "{room.id.required}") Long roomId
) {
}
