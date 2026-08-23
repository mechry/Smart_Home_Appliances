package com.smart.home.service;

import com.smart.home.domain.Fan;
import com.smart.home.domain.PowerState;
import com.smart.home.dto.FanRequest;
import com.smart.home.repository.FanRepository;
import com.smart.home.validation.FanValidator;
import com.smart.home.validation.RoomValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class FanService extends AbstractApplianceService<Fan> {

    private final FanRepository fanRepository;

    public FanService(
            FanRepository fanRepository,
            RoomService roomService,
            MessageSource messageSource,
            LockManager lockManager,
            ApplicationEventPublisher eventPublisher) {
        super(fanRepository, roomService, messageSource, lockManager, eventPublisher);
        this.fanRepository = fanRepository;
    }

    @Transactional
    public Fan createFan(FanRequest request) {
        Fan fan = new Fan(request.name(), roomService.getRoomById(request.roomId()));
        return createAppliance(request.roomId(), request.name(), fan);
    }

    public List<Fan> getAllFans() {
        return getAllAppliances();
    }

    public List<Fan> getFansByRoom(Long roomId) {
        return getAppliancesByRoom(roomId);
    }

    public Fan getFan(Long fanId) {
        return getAppliance(fanId);
    }

    @Transactional
    public Fan updateFanSpeed(Long fanId, Integer speed) {
        Fan fan = getValidatedAppliance(fanId);
        int validatedSpeed = FanValidator.validateFanSpeed(speed, messageSource);

        return lockManager.executeWithLock(fanId, () -> {
            if (validatedSpeed == 0) {
                fan.turnOff();
            } else {
                fan.setSpeed(validatedSpeed);
                fan.setPowerState(PowerState.ON);
            }
            Fan saved = fanRepository.save(fan);
            publishStateChangeEvent(saved, validatedSpeed == 0 ? "OFF" : "ON");
            return saved;
        });
    }

    @Transactional
    public Fan updateFanSpeedByRoom(Long roomId, Long fanId, Integer speed) {
        Fan fan = getValidatedAppliance(fanId);
        RoomValidator.validateRoomExists(roomService.getRoomById(roomId), roomId, messageSource);
        RoomValidator.validateDeviceInRoom(roomId, fanId, fan.getRoom().getId(), messageSource);
        int validatedSpeed = FanValidator.validateFanSpeed(speed, messageSource);

        return lockManager.executeWithLock(fanId, () -> {
            if (validatedSpeed == 0) {
                fan.turnOff();
            } else {
                fan.setSpeed(validatedSpeed);
                fan.setPowerState(PowerState.ON);
            }
            Fan saved = fanRepository.save(fan);
            publishStateChangeEvent(saved, validatedSpeed == 0 ? "OFF" : "ON");
            return saved;
        });
    }

    public void deleteFan(Long fanId) {
        deleteAppliance(fanId);
    }

    @Override
    protected Fan getValidatedAppliance(Long applianceId) {
        Fan fan = fanRepository.findById(applianceId).orElse(null);
        FanValidator.validateFanExists(fan, applianceId, messageSource);
        return fan;
    }
}
