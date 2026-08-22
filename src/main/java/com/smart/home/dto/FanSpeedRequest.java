package com.smart.home.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FanSpeedRequest(
        @NotNull(message = "{fan.speed.required}")
        @Min(value = 0, message = "{fan.speed.min}")
        @Max(value = 2, message = "{fan.speed.max}") Integer speed
) {
}
