package com.smart.home.validation;

import com.smart.home.domain.Room;
import com.smart.home.exception.ResourceNotFoundException;
import com.smart.home.repository.RoomRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

public final class RoomValidator {

    private RoomValidator() {
    }

    public static Room validateRoomExists(Room room, Long roomId, MessageSource messageSource) {
        if (room == null) {
            throw new ResourceNotFoundException(messageSource.getMessage(
                    "room.not.found",
                    new Object[] {roomId},
                    "room.not.found",
                    LocaleContextHolder.getLocale()
            ));
        }
        return room;
    }

    public static void validateRoomName(String roomName, RoomRepository roomRepository, MessageSource messageSource) {
        String normalizedName = roomName == null ? null : roomName.trim();
        if (normalizedName == null || normalizedName.isEmpty()) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    "room.name.required",
                    new Object[0],
                    "room.name.required",
                    LocaleContextHolder.getLocale()
            ));
        }

        if (roomRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    "room.name.exists",
                    new Object[] {normalizedName},
                    "room.name.exists",
                    LocaleContextHolder.getLocale()
            ));
        }
    }

    public static void validateDeviceInRoom(Long roomId, Long deviceId, Long ownerRoomId, MessageSource messageSource) {
        if (ownerRoomId == null || !ownerRoomId.equals(roomId)) {
            throw new ResourceNotFoundException(messageSource.getMessage(
                    "appliance.room.mismatch",
                    new Object[] {deviceId, roomId},
                    "appliance.room.mismatch",
                    LocaleContextHolder.getLocale()
            ));
        }
    }
}
