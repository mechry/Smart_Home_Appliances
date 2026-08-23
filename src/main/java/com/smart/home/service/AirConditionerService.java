package com.smart.home.service;

import com.smart.home.domain.AirConditioner;
import com.smart.home.domain.Room;
import com.smart.home.dto.AirConditionerRequest;
import com.smart.home.repository.AirConditionerRepository;
import com.smart.home.validation.AirConditionerValidator;
import com.smart.home.validation.RoomValidator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AirConditionerService {

    private final AirConditionerRepository airConditionerRepository;
    private final RoomService roomService;
    private final MessageSource messageSource;
    private final Map<Long, ReentrantLock> acLocks = new ConcurrentHashMap<>();

    @Transactional
    public AirConditioner createAirConditioner(AirConditionerRequest request) {
        Room room = roomService.getRoomById(request.roomId());
        RoomValidator.validateRoomExists(room, request.roomId(), messageSource);
        AirConditioner airConditioner = new AirConditioner(request.name(), room);
        room.addAppliance(airConditioner);
        log.info("Created air conditioner {} in room {}", airConditioner.getName(), room.getName());
        return airConditionerRepository.save(airConditioner);
    }

    @Transactional(readOnly = true)
    public List<AirConditioner> getAllAirConditioners() {
        return airConditionerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<AirConditioner> getAirConditionersByRoom(Long roomId) {
        Room room = roomService.getRoomById(roomId);
        RoomValidator.validateRoomExists(room, roomId, messageSource);
        return airConditionerRepository.findByRoomId(roomId);
    }

    @Transactional(readOnly = true)
    public AirConditioner getAirConditioner(Long id) {
        return getValidatedAirConditioner(id);
    }

    @Transactional
    public AirConditioner turnOn(Long id) {
        AirConditioner airConditioner = getValidatedAirConditioner(id);

        ReentrantLock lock = acLocks.computeIfAbsent(id, ignored -> new ReentrantLock());
        lock.lock();
        try {
            airConditioner.turnOn();
            return airConditionerRepository.save(airConditioner);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public AirConditioner turnOnByRoom(Long roomId, Long airConditionerId) {
        AirConditioner airConditioner = getValidatedAirConditioner(airConditionerId);
        RoomValidator.validateRoomExists(roomService.getRoomById(roomId), roomId, messageSource);

        RoomValidator.validateDeviceInRoom(roomId, airConditionerId, airConditioner.getRoom().getId(), messageSource);
        ReentrantLock lock = acLocks.computeIfAbsent(airConditionerId, ignored -> new ReentrantLock());
        lock.lock();
        try {
            airConditioner.turnOn();
            return airConditionerRepository.save(airConditioner);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public List<AirConditioner> turnOnAllByRoom(Long roomId) {
        RoomValidator.validateRoomExists(roomService.getRoomById(roomId), roomId, messageSource);

        List<AirConditioner> airConditioners = airConditionerRepository.findByRoomId(roomId);
        for (AirConditioner airConditioner : airConditioners) {
            ReentrantLock lock = acLocks.computeIfAbsent(airConditioner.getId(), ignored -> new ReentrantLock());
            lock.lock();
            try {
                airConditioner.turnOn();
                airConditionerRepository.save(airConditioner);
            } finally {
                lock.unlock();
            }
        }
        return airConditioners;
    }

    @Transactional
    public AirConditioner turnOffByRoom(Long roomId, Long airConditionerId) {
        RoomValidator.validateRoomExists(roomService.getRoomById(roomId), roomId, messageSource);
        AirConditioner airConditioner = getValidatedAirConditioner(airConditionerId);
        RoomValidator.validateDeviceInRoom(roomId, airConditionerId, airConditioner.getRoom().getId(), messageSource);
        ReentrantLock lock = acLocks.computeIfAbsent(airConditionerId, ignored -> new ReentrantLock());
        lock.lock();
        try {
            airConditioner.turnOff();
            return airConditionerRepository.save(airConditioner);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public List<AirConditioner> turnOffAllByRoom(Long roomId) {
        RoomValidator.validateRoomExists(roomService.getRoomById(roomId), roomId, messageSource);
        List<AirConditioner> airConditioners = airConditionerRepository.findByRoomId(roomId);
        for (AirConditioner airConditioner : airConditioners) {
            ReentrantLock lock = acLocks.computeIfAbsent(airConditioner.getId(), ignored -> new ReentrantLock());
            lock.lock();
            try {
                airConditioner.turnOff();
                airConditionerRepository.save(airConditioner);
            } finally {
                lock.unlock();
            }
        }
        return airConditioners;
    }

    @Transactional
    public AirConditioner turnOff(Long id) {
        AirConditioner airConditioner = getValidatedAirConditioner(id);
        ReentrantLock lock = acLocks.computeIfAbsent(id, ignored -> new ReentrantLock());
        lock.lock();
        try {
            airConditioner.turnOff();
            return airConditionerRepository.save(airConditioner);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public void shutdownAllAirConditioners() {
        for (AirConditioner airConditioner : airConditionerRepository.findAll()) {
            ReentrantLock lock = acLocks.computeIfAbsent(airConditioner.getId(), ignored -> new ReentrantLock());
            lock.lock();
            try {
                airConditioner.turnOff();
                airConditionerRepository.save(airConditioner);
            } finally {
                lock.unlock();
            }
        }
    }

    @Transactional
    public void deleteAirConditioner(Long airConditionerId) {
        AirConditioner airConditioner = getValidatedAirConditioner(airConditionerId);
        Room room = airConditioner.getRoom();
        room.removeAppliance(airConditioner);
        acLocks.remove(airConditionerId);
        airConditionerRepository.delete(airConditioner);
        log.info("Deleted air conditioner {} from room {}", airConditioner.getName(), room.getName());
    }

    private AirConditioner getValidatedAirConditioner(Long id) {
        AirConditioner airConditioner = airConditionerRepository.findById(id).orElse(null);
        AirConditionerValidator.validateAirConditionerExists(airConditioner, id, messageSource);
        return airConditioner;
    }
}
