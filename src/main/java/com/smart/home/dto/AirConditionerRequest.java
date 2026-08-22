package com.smart.home.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AirConditionerRequest(
        @NotBlank(message = "{air.conditioner.name.required}") String name,
        @NotNull(message = "{room.id.required}") Long roomId
) {
}
