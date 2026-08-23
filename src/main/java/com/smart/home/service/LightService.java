package com.smart.home.service;

import com.smart.home.domain.Light;
import com.smart.home.dto.LightRequest;
import com.smart.home.repository.LightRepository;
import com.smart.home.validation.LightValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class LightService extends AbstractApplianceService<Light> {

    private final LightRepository lightRepository;

    public LightService(
            LightRepository lightRepository,
            RoomService roomService,
            MessageSource messageSource,
            LockManager lockManager,
            ApplicationEventPublisher eventPublisher) {
        super(lightRepository, roomService, messageSource, lockManager, eventPublisher);
        this.lightRepository = lightRepository;
    }

    public Light createLight(LightRequest request) {
        Light light = new Light(request.name(), roomService.getRoomById(request.roomId()));
        return createAppliance(request.roomId(), request.name(), light);
    }

    public List<Light> getAllLights() {
        return getAllAppliances();
    }

    public List<Light> getLightsByRoom(Long roomId) {
        return getAppliancesByRoom(roomId);
    }

    public Light getLight(Long lightId) {
        return getAppliance(lightId);
    }

    public void deleteLight(Long lightId) {
        deleteAppliance(lightId);
    }

    @Override
    protected Light getValidatedAppliance(Long applianceId) {
        Light light = lightRepository.findById(applianceId).orElse(null);
        LightValidator.validateLightExists(light, applianceId, messageSource);
        return light;
    }
}
