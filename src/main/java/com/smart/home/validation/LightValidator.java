package com.smart.home.validation;

import com.smart.home.domain.Light;
import com.smart.home.exception.ResourceNotFoundException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

public final class LightValidator {

    private LightValidator() {
    }

    public static void validateLightExists(Light light, Long lightId, MessageSource messageSource) {
        if (light == null) {
            throw new ResourceNotFoundException(messageSource.getMessage(
                    "light.not.found",
                    new Object[] {lightId},
                    "light.not.found",
                    LocaleContextHolder.getLocale()
            ));
        }
    }
}
