package com.smart.home.service;

import com.smart.home.domain.Light;
import com.smart.home.domain.Room;
import com.smart.home.dto.LightRequest;
import com.smart.home.repository.LightRepository;
import com.smart.home.validation.LightValidator;
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
public class LightService {

    private final LightRepository lightRepository;
    private final RoomService roomService;
    private final MessageSource messageSource;
    private final Map<Long, ReentrantLock> lightLocks = new ConcurrentHashMap<>();

    @Transactional
    public Light createLight(LightRequest request) {
        Room room = roomService.getRoomById(request.roomId());
        RoomValidator.validateRoomExists(room, request.roomId(), messageSource);
        Light light = new Light(request.name(), room);
        room.addAppliance(light);
        log.info("Created light {} in room {}", light.getName(), room.getName());
        return lightRepository.save(light);
    }

    @Transactional(readOnly = true)
    public List<Light> getAllLights() {
        return lightRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Light> getLightsByRoom(Long roomId) {
        return lightRepository.findByRoomId(roomId);
    }

    @Transactional(readOnly = true)
    public Light getLight(Long lightId) {
        return getValidatedLight(lightId);
    }

    @Transactional
    public Light turnOn(Long lightId) {
        Light light = getValidatedLight(lightId);
        ReentrantLock lock = lightLocks.computeIfAbsent(lightId, ignored -> new ReentrantLock());
        lock.lock();
        try {
            light.turnOn();
            return lightRepository.save(light);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public Light turnOnByRoom(Long roomId, Long lightId) {
        Light light = getValidatedLight(lightId);
        RoomValidator.validateRoomExists(roomService.getRoomById(roomId), roomId, messageSource);
        RoomValidator.validateDeviceInRoom(roomId, lightId, light.getRoom().getId(), messageSource);
        ReentrantLock lock = lightLocks.computeIfAbsent(lightId, ignored -> new ReentrantLock());
        lock.lock();
        try {
            light.turnOn();
            return lightRepository.save(light);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public List<Light> turnOnAllByRoom(Long roomId) {
        RoomValidator.validateRoomExists(roomService.getRoomById(roomId), roomId, messageSource);
        List<Light> lights = lightRepository.findByRoomId(roomId);
        for (Light light : lights) {
            ReentrantLock lock = lightLocks.computeIfAbsent(light.getId(), ignored -> new ReentrantLock());
            lock.lock();
            try {
                light.turnOn();
                lightRepository.save(light);
            } finally {
                lock.unlock();
            }
        }
        return lights;
    }

    @Transactional
    public Light turnOffByRoom(Long roomId, Long lightId) {
        Light light = getValidatedLight(lightId);
        RoomValidator.validateRoomExists(roomService.getRoomById(roomId), roomId, messageSource);
        RoomValidator.validateDeviceInRoom(roomId, lightId, light.getRoom().getId(), messageSource);

        ReentrantLock lock = lightLocks.computeIfAbsent(lightId, ignored -> new ReentrantLock());
        lock.lock();
        try {
            light.turnOff();
            return lightRepository.save(light);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public List<Light> turnOffAllByRoom(Long roomId) {
        RoomValidator.validateRoomExists(roomService.getRoomById(roomId), roomId, messageSource);
        List<Light> lights = lightRepository.findByRoomId(roomId);
        for (Light light : lights) {
            ReentrantLock lock = lightLocks.computeIfAbsent(light.getId(), ignored -> new ReentrantLock());
            lock.lock();
            try {
                light.turnOff();
                lightRepository.save(light);
            } finally {
                lock.unlock();
            }
        }
        return lights;
    }

    @Transactional
    public Light turnOff(Long lightId) {
        Light light = getValidatedLight(lightId);
        ReentrantLock lock = lightLocks.computeIfAbsent(lightId, ignored -> new ReentrantLock());
        lock.lock();
        try {
            light.turnOff();
            return lightRepository.save(light);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public void shutdownAllLights() {
        for (Light light : lightRepository.findAll()) {
            ReentrantLock lock = lightLocks.computeIfAbsent(light.getId(), ignored -> new ReentrantLock());
            lock.lock();
            try {
                light.turnOff();
                lightRepository.save(light);
            } finally {
                lock.unlock();
            }
        }
    }

    @Transactional
    public void deleteLight(Long lightId) {
        Light light = getValidatedLight(lightId);
        Room room = light.getRoom();
        room.removeAppliance(light);
        lightLocks.remove(lightId);
        lightRepository.delete(light);
        log.info("Deleted light {} from room {}", light.getName(), room.getName());
    }

    private Light getValidatedLight(Long lightId) {
        Light light = lightRepository.findById(lightId).orElse(null);
        LightValidator.validateLightExists(light, lightId, messageSource);
        return light;
    }
}
