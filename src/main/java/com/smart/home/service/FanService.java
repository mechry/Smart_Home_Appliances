package com.smart.home.service;

import com.smart.home.domain.Fan;
import com.smart.home.domain.PowerState;
import com.smart.home.domain.Room;
import com.smart.home.dto.FanRequest;
import com.smart.home.repository.FanRepository;
import com.smart.home.validation.FanValidator;
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
public class FanService {

    private final FanRepository fanRepository;
    private final RoomService roomService;
    private final MessageSource messageSource;
    private final Map<Long, ReentrantLock> fanLocks = new ConcurrentHashMap<>();

    @Transactional
    public Fan createFan(FanRequest request) {
        Room room = roomService.getRoomById(request.roomId());
        RoomValidator.validateRoomExists(room, request.roomId(), messageSource);
        Fan fan = new Fan(request.name(), room);
        room.addAppliance(fan);
        log.info("Created fan {} in room {}", fan.getName(), room.getName());
        return fanRepository.save(fan);
    }

    @Transactional(readOnly = true)
    public List<Fan> getAllFans() {
        return fanRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Fan> getFansByRoom(Long roomId) {
        return fanRepository.findByRoomId(roomId);
    }

    @Transactional(readOnly = true)
    public Fan getFan(Long fanId) {
        return getValidatedFan(fanId);
    }

    @Transactional
    public Fan updateFanSpeed(Long fanId, Integer speed) {
        Fan fan = getValidatedFan(fanId);
        int validatedSpeed = FanValidator.validateFanSpeed(speed, messageSource);

        ReentrantLock lock = fanLocks.computeIfAbsent(fanId, ignored -> new ReentrantLock());
        lock.lock();
        try {
            if (validatedSpeed == 0) {
                fan.turnOff();
            } else {
                fan.setSpeed(validatedSpeed);
                fan.setPowerState(PowerState.ON);
            }
            return fanRepository.save(fan);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public Fan updateFanSpeedByRoom(Long roomId, Long fanId, Integer speed) {
        Fan fan = getValidatedFan(fanId);
        RoomValidator.validateRoomExists(roomService.getRoomById(roomId), roomId, messageSource);
        RoomValidator.validateDeviceInRoom(roomId, fanId, fan.getRoom().getId(), messageSource);
        int validatedSpeed = FanValidator.validateFanSpeed(speed, messageSource);

        ReentrantLock lock = fanLocks.computeIfAbsent(fanId, ignored -> new ReentrantLock());
        lock.lock();
        try {
            if (validatedSpeed == 0) {
                fan.turnOff();
            } else {
                fan.setSpeed(validatedSpeed);
                fan.setPowerState(PowerState.ON);
            }
            return fanRepository.save(fan);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public Fan turnOn(Long fanId) {
        Fan fan = getValidatedFan(fanId);
        ReentrantLock lock = fanLocks.computeIfAbsent(fanId, ignored -> new ReentrantLock());
        lock.lock();
        try {
            fan.turnOn();
            return fanRepository.save(fan);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public Fan turnOnByRoom(Long roomId, Long fanId) {
        Fan fan = getValidatedFan(fanId);
        RoomValidator.validateRoomExists(roomService.getRoomById(roomId), roomId, messageSource);
        RoomValidator.validateDeviceInRoom(roomId, fanId, fan.getRoom().getId(), messageSource);

        ReentrantLock lock = fanLocks.computeIfAbsent(fanId, ignored -> new ReentrantLock());
        lock.lock();
        try {
            fan.turnOn();
            return fanRepository.save(fan);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public List<Fan> turnOnAllByRoom(Long roomId) {
        RoomValidator.validateRoomExists(roomService.getRoomById(roomId), roomId, messageSource);
        List<Fan> fans = fanRepository.findByRoomId(roomId);
        for (Fan fan : fans) {
            ReentrantLock lock = fanLocks.computeIfAbsent(fan.getId(), ignored -> new ReentrantLock());
            lock.lock();
            try {
                fan.turnOn();
                fanRepository.save(fan);
            } finally {
                lock.unlock();
            }
        }
        return fans;
    }

    @Transactional
    public Fan turnOffByRoom(Long roomId, Long fanId) {
        Fan fan = getValidatedFan(fanId);
        RoomValidator.validateRoomExists(roomService.getRoomById(roomId), roomId, messageSource);
        RoomValidator.validateDeviceInRoom(roomId, fanId, fan.getRoom().getId(), messageSource);

        ReentrantLock lock = fanLocks.computeIfAbsent(fanId, ignored -> new ReentrantLock());
        lock.lock();
        try {
            fan.turnOff();
            return fanRepository.save(fan);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public List<Fan> turnOffAllByRoom(Long roomId) {
        RoomValidator.validateRoomExists(roomService.getRoomById(roomId), roomId, messageSource);
        List<Fan> fans = fanRepository.findByRoomId(roomId);
        for (Fan fan : fans) {
            ReentrantLock lock = fanLocks.computeIfAbsent(fan.getId(), ignored -> new ReentrantLock());
            lock.lock();
            try {
                fan.turnOff();
                fanRepository.save(fan);
            } finally {
                lock.unlock();
            }
        }
        return fans;
    }

    @Transactional
    public Fan turnOff(Long fanId) {
        Fan fan = getValidatedFan(fanId);
        ReentrantLock lock = fanLocks.computeIfAbsent(fanId, ignored -> new ReentrantLock());
        lock.lock();
        try {
            fan.turnOff();
            return fanRepository.save(fan);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public void shutdownAllFans() {
        for (Fan fan : fanRepository.findAll()) {
            ReentrantLock lock = fanLocks.computeIfAbsent(fan.getId(), ignored -> new ReentrantLock());
            lock.lock();
            try {
                fan.turnOff();
                fanRepository.save(fan);
            } finally {
                lock.unlock();
            }
        }
    }

    @Transactional
    public void deleteFan(Long fanId) {
        Fan fan = getValidatedFan(fanId);
        Room room = fan.getRoom();
        room.removeAppliance(fan);
        fanLocks.remove(fanId);
        fanRepository.delete(fan);
        log.info("Deleted fan {} from room {}", fan.getName(), room.getName());
    }

    private Fan getValidatedFan(Long fanId) {
        Fan fan = fanRepository.findById(fanId).orElse(null);
        FanValidator.validateFanExists(fan, fanId, messageSource);
        return fan;
    }
}
