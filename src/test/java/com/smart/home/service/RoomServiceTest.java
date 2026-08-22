package com.smart.home.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.smart.home.domain.Room;
import com.smart.home.dto.RoomRequest;
import com.smart.home.exception.ResourceNotFoundException;
import com.smart.home.repository.RoomRepository;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;

class RoomServiceTest {

    @Test
    void createRoomSavesUniqueRoomName() {
        RoomRepository roomRepository = Mockito.mock(RoomRepository.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);
        RoomRequest request = new RoomRequest("  Kitchen  ");
        Room savedRoom = new Room("Kitchen");

        Mockito.when(roomRepository.existsByNameIgnoreCase("Kitchen")).thenReturn(false);
        Mockito.when(roomRepository.save(Mockito.any(Room.class))).thenReturn(savedRoom);

        RoomService roomService = new RoomService(roomRepository, messageSource);
        Room result = roomService.createRoom(request);

        assertThat(result.getName()).isEqualTo("Kitchen");
        Mockito.verify(roomRepository).save(Mockito.any(Room.class));
    }

    @Test
    void createRoomRejectsBlankRoomName() {
        RoomRepository roomRepository = Mockito.mock(RoomRepository.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);
        Mockito.when(messageSource.getMessage(Mockito.eq("room.name.required"), Mockito.any(Object[].class), Mockito.eq("room.name.required"), Mockito.any()))
                .thenReturn("Room name is required");

        RoomService roomService = new RoomService(roomRepository, messageSource);

        assertThatThrownBy(() -> roomService.createRoom(new RoomRequest("   ")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Room name is required");
    }

    @Test
    void createRoomRejectsDuplicateName() {
        RoomRepository roomRepository = Mockito.mock(RoomRepository.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);
        Mockito.when(roomRepository.existsByNameIgnoreCase("Bedroom")).thenReturn(true);
        Mockito.when(messageSource.getMessage(Mockito.eq("room.name.exists"), Mockito.any(Object[].class), Mockito.eq("room.name.exists"), Mockito.any()))
                .thenReturn("Room name 'Bedroom' already exists");

        RoomService roomService = new RoomService(roomRepository, messageSource);

        assertThatThrownBy(() -> roomService.createRoom(new RoomRequest("Bedroom")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Room name 'Bedroom' already exists");
    }

    @Test
    void getAllRoomsReturnsAllRooms() {
        RoomRepository roomRepository = Mockito.mock(RoomRepository.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);
        List<Room> rooms = List.of(new Room("Living Room"), new Room("Bedroom"));

        Mockito.when(roomRepository.findAll()).thenReturn(rooms);

        RoomService roomService = new RoomService(roomRepository, messageSource);

        assertThat(roomService.getAllRooms()).hasSize(2)
                .extracting(Room::getName)
                .containsExactly("Living Room", "Bedroom");
    }

    @Test
    void getRoomByIdReturnsExistingRoom() {
        RoomRepository roomRepository = Mockito.mock(RoomRepository.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);
        Room room = new Room("Office");

        Mockito.when(roomRepository.findById(2L)).thenReturn(Optional.of(room));

        RoomService roomService = new RoomService(roomRepository, messageSource);

        assertThat(roomService.getRoomById(2L)).isEqualTo(room);
    }

    @Test
    void roomServiceThrowsResourceNotFoundWhenRoomDoesNotExist() {
        RoomRepository roomRepository = Mockito.mock(RoomRepository.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);
        Mockito.when(roomRepository.findById(999L)).thenReturn(Optional.empty());
        Mockito.when(messageSource.getMessage(Mockito.eq("room.not.found"), Mockito.any(Object[].class), Mockito.eq("room.not.found"), Mockito.any()))
                .thenReturn("Room not found with id 999");

        RoomService roomService = new RoomService(roomRepository, messageSource);

        assertThatThrownBy(() -> roomService.getRoomById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Room not found with id 999");
    }
}
