package com.smart.home.service;

import com.smart.home.domain.AirConditioner;
import com.smart.home.dto.AirConditionerRequest;
import com.smart.home.repository.AirConditionerRepository;
import com.smart.home.validation.AirConditionerValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class AirConditionerService extends AbstractApplianceService<AirConditioner> {

    private final AirConditionerRepository airConditionerRepository;

    public AirConditionerService(
            AirConditionerRepository airConditionerRepository,
            RoomService roomService,
            MessageSource messageSource,
            LockManager lockManager,
            ApplicationEventPublisher eventPublisher) {
        super(airConditionerRepository, roomService, messageSource, lockManager, eventPublisher);
        this.airConditionerRepository = airConditionerRepository;
    }

    @Transactional
    public AirConditioner createAirConditioner(AirConditionerRequest request) {
        AirConditioner airConditioner = new AirConditioner(request.name(), roomService.getRoomById(request.roomId()));
        return createAppliance(request.roomId(), request.name(), airConditioner);
    }

    public List<AirConditioner> getAllAirConditioners() {
        return getAllAppliances();
    }

    public List<AirConditioner> getAirConditionersByRoom(Long roomId) {
        return getAppliancesByRoom(roomId);
    }

    public AirConditioner getAirConditioner(Long id) {
        return getAppliance(id);
    }

    public void deleteAirConditioner(Long airConditionerId) {
        deleteAppliance(airConditionerId);
    }

    @Override
    protected AirConditioner getValidatedAppliance(Long applianceId) {
        AirConditioner airConditioner = airConditionerRepository.findById(applianceId).orElse(null);
        AirConditionerValidator.validateAirConditionerExists(airConditioner, applianceId, messageSource);
        return airConditioner;
    }
}
