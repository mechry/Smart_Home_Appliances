package com.smart.home.service;

import com.smart.home.domain.Appliance;
import com.smart.home.domain.Room;
import com.smart.home.event.ApplianceStateChangedEvent;
import com.smart.home.repository.ApplianceRepository;
import com.smart.home.validation.RoomValidator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
public abstract class AbstractApplianceService<T extends Appliance> implements ApplianceManager {

    protected final ApplianceRepository<T> repository;
    protected final RoomService roomService;
    protected final MessageSource messageSource;
    protected final LockManager lockManager;
    protected final ApplicationEventPublisher eventPublisher;

    protected AbstractApplianceService(
            ApplianceRepository<T> repository,
            RoomService roomService,
            MessageSource messageSource,
            LockManager lockManager,
            ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.roomService = roomService;
        this.messageSource = messageSource;
        this.lockManager = lockManager;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public T createAppliance(Long roomId, String applianceName, T appliance) {
        Room room = roomService.getRoomById(roomId);
        RoomValidator.validateRoomExists(room, roomId, messageSource);
        room.addAppliance(appliance);
        log.info("Created {} {} in room {}", appliance.getClass().getSimpleName(), appliance.getName(), room.getName());
        return repository.save(appliance);
    }

    @Transactional(readOnly = true)
    public List<T> getAllAppliances() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<T> getAppliancesByRoom(Long roomId) {
        return repository.findByRoomId(roomId);
    }

    @Transactional(readOnly = true)
    public T getAppliance(Long applianceId) {
        return getValidatedAppliance(applianceId);
    }

    @Transactional
    public T turnOn(Long applianceId) {
        T appliance = getValidatedAppliance(applianceId);
        return lockManager.executeWithLock(applianceId, () -> {
            appliance.turnOn();
            T saved = repository.save(appliance);
            publishStateChangeEvent(saved, "ON");
            return saved;
        });
    }

    @Transactional
    public T turnOnByRoom(Long roomId, Long applianceId) {
        T appliance = getValidatedAppliance(applianceId);
        RoomValidator.validateRoomExists(roomService.getRoomById(roomId), roomId, messageSource);
        RoomValidator.validateDeviceInRoom(roomId, applianceId, appliance.getRoom().getId(), messageSource);
        
        return lockManager.executeWithLock(applianceId, () -> {
            appliance.turnOn();
            T saved = repository.save(appliance);
            publishStateChangeEvent(saved, "ON");
            return saved;
        });
    }

    @Transactional
    public List<T> turnOnAllByRoom(Long roomId) {
        RoomValidator.validateRoomExists(roomService.getRoomById(roomId), roomId, messageSource);
        List<T> appliances = repository.findByRoomId(roomId);
        
        for (T appliance : appliances) {
            lockManager.executeWithLock(appliance.getId(), () -> {
                appliance.turnOn();
                T saved = repository.save(appliance);
                publishStateChangeEvent(saved, "ON");
            });
        }
        return appliances;
    }

    @Transactional
    public T turnOff(Long applianceId) {
        T appliance = getValidatedAppliance(applianceId);
        return lockManager.executeWithLock(applianceId, () -> {
            appliance.turnOff();
            T saved = repository.save(appliance);
            publishStateChangeEvent(saved, "OFF");
            return saved;
        });
    }

    @Transactional
    public T turnOffByRoom(Long roomId, Long applianceId) {
        T appliance = getValidatedAppliance(applianceId);
        RoomValidator.validateRoomExists(roomService.getRoomById(roomId), roomId, messageSource);
        RoomValidator.validateDeviceInRoom(roomId, applianceId, appliance.getRoom().getId(), messageSource);
        
        return lockManager.executeWithLock(applianceId, () -> {
            appliance.turnOff();
            T saved = repository.save(appliance);
            publishStateChangeEvent(saved, "OFF");
            return saved;
        });
    }

    @Transactional
    public List<T> turnOffAllByRoom(Long roomId) {
        RoomValidator.validateRoomExists(roomService.getRoomById(roomId), roomId, messageSource);
        List<T> appliances = repository.findByRoomId(roomId);
        
        for (T appliance : appliances) {
            lockManager.executeWithLock(appliance.getId(), () -> {
                appliance.turnOff();
                T saved = repository.save(appliance);
                publishStateChangeEvent(saved, "OFF");
            });
        }
        return appliances;
    }

    @Transactional
    public void shutdownAll() {
        for (T appliance : repository.findAll()) {
            lockManager.executeWithLock(appliance.getId(), () -> {
                appliance.turnOff();
                repository.save(appliance);
                publishStateChangeEvent(appliance, "OFF");
            });
        }
    }

    @Transactional
    public void deleteAppliance(Long applianceId) {
        T appliance = getValidatedAppliance(applianceId);
        Room room = appliance.getRoom();
        room.removeAppliance(appliance);
        lockManager.removeLock(applianceId);
        repository.delete(appliance);
        log.info("Deleted {} {} from room {}", appliance.getClass().getSimpleName(), appliance.getName(), room.getName());
    }

    protected abstract T getValidatedAppliance(Long applianceId);

    protected void publishStateChangeEvent(T appliance, String newState) {
        eventPublisher.publishEvent(new ApplianceStateChangedEvent(this, appliance, newState));
    }
}
