package com.smart.home.service;

import com.smart.home.domain.Room;
import com.smart.home.dto.RoomRequest;
import com.smart.home.repository.RoomRepository;
import com.smart.home.validation.RoomValidator;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {

    private final RoomRepository roomRepository;
    private final MessageSource messageSource;

    @Transactional
    public Room createRoom(RoomRequest request) {
        RoomValidator.validateRoomName(request.name(), roomRepository, messageSource);

        String roomName = request.name().trim();
        log.info("Creating room with name {}", roomName);
        return roomRepository.save(new Room(roomName));
    }

    @Transactional(readOnly = true)
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Room getRoomById(Long roomId) {
        Room room = roomRepository.findById(roomId).orElse(null);
        return RoomValidator.validateRoomExists(room, roomId, messageSource);
    }

    private String message(String code, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, args, code, locale);
    }
}
