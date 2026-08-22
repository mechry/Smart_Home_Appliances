package com.smart.home.validation;

import com.smart.home.domain.AirConditioner;
import com.smart.home.exception.ResourceNotFoundException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

public final class AirConditionerValidator {

    private AirConditionerValidator() {
    }

    public static void validateAirConditionerExists(AirConditioner airConditioner, Long airConditionerId, MessageSource messageSource) {
        if (airConditioner == null) {
            throw new ResourceNotFoundException(messageSource.getMessage(
                    "air.conditioner.not.found",
                    new Object[] {airConditionerId},
                    "air.conditioner.not.found",
                    LocaleContextHolder.getLocale()
            ));
        }
    }
}
