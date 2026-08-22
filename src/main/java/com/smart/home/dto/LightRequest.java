package com.smart.home.dto;

import com.smart.home.domain.Light;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LightRequest(
        @NotBlank(message = "{light.name.required}") String name,
        @NotNull(message = "{room.id.required}") Long roomId
) {
}
