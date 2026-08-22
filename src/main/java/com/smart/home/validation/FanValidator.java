package com.smart.home.validation;

import com.smart.home.domain.Fan;
import com.smart.home.exception.ResourceNotFoundException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

public final class FanValidator {

    private FanValidator() {
    }

    public static void validateFanExists(Fan fan, Long fanId, MessageSource messageSource) {
        if (fan == null) {
            throw new ResourceNotFoundException(messageSource.getMessage(
                    "fan.not.found",
                    new Object[] {fanId},
                    "fan.not.found",
                    LocaleContextHolder.getLocale()
            ));
        }
    }

    public static int validateFanSpeed(Integer speed, MessageSource messageSource) {
        if (speed == null || speed < 0 || speed > 2) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    "fan.speed.invalid",
                    new Object[0],
                    "fan.speed.invalid",
                    LocaleContextHolder.getLocale()
            ));
        }
        return speed;
    }
}
