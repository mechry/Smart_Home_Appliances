package com.smart.home.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.smart.home.domain.Fan;
import com.smart.home.domain.PowerState;
import com.smart.home.domain.Room;
import com.smart.home.dto.FanRequest;
import com.smart.home.exception.ResourceNotFoundException;
import com.smart.home.repository.FanRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;

class FanServiceTest {

    @Test
    void createFanSavesFan() {
        FanRepository fanRepository = Mockito.mock(FanRepository.class);
        RoomService roomService = Mockito.mock(RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Room room = new Room("Living Room");
        FanRequest request = new FanRequest("Ceiling Fan", 1L);
        Fan saved = new Fan("Ceiling Fan", room);

        Mockito.when(roomService.getRoomById(1L)).thenReturn(room);
        Mockito.when(fanRepository.save(Mockito.any(Fan.class))).thenReturn(saved);

        FanService svc = new FanService(fanRepository, roomService, messageSource);
        Fan result = svc.createFan(request);

        assertThat(result.getName()).isEqualTo("Ceiling Fan");
    }

    @Test
    void getAllFansReturnsAll() {
        FanRepository fanRepository = Mockito.mock(FanRepository.class);
        RoomService roomService = Mockito.mock(RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        List<Fan> fans = List.of(
                new Fan("Fan1", new Room("R1")),
                new Fan("Fan2", new Room("R2"))
        );
        Mockito.when(fanRepository.findAll()).thenReturn(fans);

        FanService svc = new FanService(fanRepository, roomService, messageSource);
        assertThat(svc.getAllFans()).hasSize(2)
                .extracting(Fan::getName)
                .containsExactly("Fan1", "Fan2");
    }

    @Test
    void getFansByRoomReturnsList() {
        FanRepository fanRepository = Mockito.mock(FanRepository.class);
        RoomService roomService = Mockito.mock(RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Room room = new Room("Bedroom");
        List<Fan> fans = List.of(new Fan("Bedroom Fan", room));

        Mockito.when(roomService.getRoomById(5L)).thenReturn(room);
        Mockito.when(fanRepository.findByRoomId(5L)).thenReturn(fans);

        FanService svc = new FanService(fanRepository, roomService, messageSource);
        assertThat(svc.getFansByRoom(5L)).hasSize(1)
                .extracting(Fan::getName)
                .containsExactly("Bedroom Fan");
    }

    @Test
    void getFanByIdReturnsExistingFan() {
        FanRepository fanRepository = Mockito.mock(FanRepository.class);
        RoomService roomService = Mockito.mock(RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Fan fan = new Fan("Office Fan", new Room("Office"));
        Mockito.when(fanRepository.findById(3L)).thenReturn(Optional.of(fan));

        FanService svc = new FanService(fanRepository, roomService, messageSource);
        assertThat(svc.getFan(3L)).isEqualTo(fan);
    }

    @Test
    void getFanByIdThrowsNotFound() {
        FanRepository fanRepository = Mockito.mock(FanRepository.class);
        RoomService roomService = Mockito.mock(RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Mockito.when(fanRepository.findById(999L)).thenReturn(Optional.empty());
        Mockito.when(messageSource.getMessage(
                Mockito.eq("fan.not.found"),
                Mockito.any(Object[].class),
                Mockito.eq("fan.not.found"),
                Mockito.any()
        )).thenReturn("Fan not found with id 999");

        FanService svc = new FanService(fanRepository, roomService, messageSource);

        assertThatThrownBy(() -> svc.getFan(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Fan not found with id 999");
    }

    @Test
    void turnOnTurnsFanOn() {
        FanRepository fanRepository = Mockito.mock(FanRepository.class);
        RoomService roomService = Mockito.mock(RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Room room = new Room("Hall");
        Fan fan = new Fan("Hall Fan", room);
        Mockito.when(fanRepository.findById(7L)).thenReturn(Optional.of(fan));
        Mockito.when(fanRepository.save(Mockito.any(Fan.class))).thenAnswer(inv -> inv.getArgument(0));

        FanService svc = new FanService(fanRepository, roomService, messageSource);
        Fan res = svc.turnOn(7L);

        assertThat(res.getPowerState()).isEqualTo(PowerState.ON);
        assertThat(res.getSpeed()).isEqualTo(1);
    }

    @Test
    void turnOffTurnsFanOff() {
        FanRepository fanRepository = Mockito.mock(FanRepository.class);
        RoomService roomService = Mockito.mock(RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Room room = new Room("Hall");
        Fan fan = new Fan("Hall Fan", room);
        fan.turnOn();

        Mockito.when(fanRepository.findById(8L)).thenReturn(Optional.of(fan));
        Mockito.when(fanRepository.save(Mockito.any(Fan.class))).thenAnswer(inv -> inv.getArgument(0));

        FanService svc = new FanService(fanRepository, roomService, messageSource);
        Fan res = svc.turnOff(8L);

        assertThat(res.getPowerState()).isEqualTo(PowerState.OFF);
        assertThat(res.getSpeed()).isEqualTo(0);
    }

    @Test
    void updateFanSpeedUpdatesSpeed() {
        FanRepository fanRepository = Mockito.mock(FanRepository.class);
        RoomService roomService = Mockito.mock(RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Room room = new Room("Living Room");
        Fan fan = new Fan("Living Room Fan", room);
        fan.setId(10L);

        Mockito.when(fanRepository.findById(10L)).thenReturn(Optional.of(fan));
        Mockito.when(fanRepository.save(Mockito.any(Fan.class))).thenAnswer(inv -> inv.getArgument(0));

        FanService svc = new FanService(fanRepository, roomService, messageSource);
        Fan res = svc.updateFanSpeed(10L, 2);

        assertThat(res.getSpeed()).isEqualTo(2);
        assertThat(res.getPowerState()).isEqualTo(PowerState.ON);
    }

    @Test
    void updateFanSpeedToZeroTurnsOff() {
        FanRepository fanRepository = Mockito.mock(FanRepository.class);
        RoomService roomService = Mockito.mock(RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Room room = new Room("Living Room");
        Fan fan = new Fan("Living Room Fan", room);
        fan.setId(10L);
        fan.turnOn();

        Mockito.when(fanRepository.findById(10L)).thenReturn(Optional.of(fan));
        Mockito.when(fanRepository.save(Mockito.any(Fan.class))).thenAnswer(inv -> inv.getArgument(0));

        FanService svc = new FanService(fanRepository, roomService, messageSource);
        Fan res = svc.updateFanSpeed(10L, 0);

        assertThat(res.getSpeed()).isEqualTo(0);
        assertThat(res.getPowerState()).isEqualTo(PowerState.OFF);
    }

    @Test
    void updateFanSpeedByRoomUpdatesSpeed() {
        FanRepository fanRepository = Mockito.mock(FanRepository.class);
        RoomService roomService = Mockito.mock(RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Room room = new Room("Kitchen");
        Fan fan = new Fan("Kitchen Fan", room);
        fan.setId(10L);

        Mockito.when(roomService.getRoomById(5L)).thenReturn(room);
        Mockito.when(fanRepository.findById(10L)).thenReturn(Optional.of(fan));
        Mockito.when(fanRepository.save(Mockito.any(Fan.class))).thenAnswer(inv -> inv.getArgument(0));

        FanService svc = new FanService(fanRepository, roomService, messageSource);
        Fan res = svc.updateFanSpeedByRoom(5L, 10L, 2);

        assertThat(res.getSpeed()).isEqualTo(2);
        assertThat(res.getPowerState()).isEqualTo(PowerState.ON);
    }

    @Test
    void turnOnByRoomTurnsFanOn() {
        FanRepository fanRepository = Mockito.mock(FanRepository.class);
        RoomService roomService = Mockito.mock(RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Room room = new Room("Kitchen");
        Fan fan = new Fan("Kitchen Fan", room);
        fan.setId(10L);

        Mockito.when(roomService.getRoomById(5L)).thenReturn(room);
        Mockito.when(fanRepository.findById(10L)).thenReturn(Optional.of(fan));
        Mockito.when(fanRepository.save(Mockito.any(Fan.class))).thenAnswer(inv -> inv.getArgument(0));

        FanService svc = new FanService(fanRepository, roomService, messageSource);
        Fan res = svc.turnOnByRoom(5L, 10L);

        assertThat(res.getPowerState()).isEqualTo(PowerState.ON);
        assertThat(res.getSpeed()).isEqualTo(1);
    }

    @Test
    void turnOnAllByRoomTurnsAllOn() {
        FanRepository fanRepository = Mockito.mock(FanRepository.class);
        RoomService roomService = Mockito.mock(RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Room room = new Room("Living Room");
        Fan fan1 = new Fan("Fan1", room);
        fan1.setId(1L);
        Fan fan2 = new Fan("Fan2", room);
        fan2.setId(2L);
        List<Fan> fans = List.of(fan1, fan2);

        Mockito.when(roomService.getRoomById(3L)).thenReturn(room);
        Mockito.when(fanRepository.findByRoomId(3L)).thenReturn(fans);
        Mockito.when(fanRepository.save(Mockito.any(Fan.class))).thenAnswer(inv -> inv.getArgument(0));

        FanService svc = new FanService(fanRepository, roomService, messageSource);
        List<Fan> result = svc.turnOnAllByRoom(3L);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(f -> f.getPowerState() == PowerState.ON);
    }

    @Test
    void turnOffByRoomTurnsFanOff() {
        FanRepository fanRepository = Mockito.mock(FanRepository.class);
        RoomService roomService = Mockito.mock(RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Room room = new Room("Kitchen");
        Fan fan = new Fan("Kitchen Fan", room);
        fan.setId(10L);
        fan.turnOn();

        Mockito.when(roomService.getRoomById(5L)).thenReturn(room);
        Mockito.when(fanRepository.findById(10L)).thenReturn(Optional.of(fan));
        Mockito.when(fanRepository.save(Mockito.any(Fan.class))).thenAnswer(inv -> inv.getArgument(0));

        FanService svc = new FanService(fanRepository, roomService, messageSource);
        Fan res = svc.turnOffByRoom(5L, 10L);

        assertThat(res.getPowerState()).isEqualTo(PowerState.OFF);
        assertThat(res.getSpeed()).isEqualTo(0);
    }

    @Test
    void turnOffAllByRoomTurnsAllOff() {
        FanRepository fanRepository = Mockito.mock(FanRepository.class);
        RoomService roomService = Mockito.mock(RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Room room = new Room("Living Room");
        Fan fan1 = new Fan("Fan1", room);
        fan1.setId(1L);
        fan1.turnOn();
        Fan fan2 = new Fan("Fan2", room);
        fan2.setId(2L);
        fan2.turnOn();
        List<Fan> fans = List.of(fan1, fan2);

        Mockito.when(roomService.getRoomById(3L)).thenReturn(room);
        Mockito.when(fanRepository.findByRoomId(3L)).thenReturn(fans);
        Mockito.when(fanRepository.save(Mockito.any(Fan.class))).thenAnswer(inv -> inv.getArgument(0));

        FanService svc = new FanService(fanRepository, roomService, messageSource);
        List<Fan> result = svc.turnOffAllByRoom(3L);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(f -> f.getPowerState() == PowerState.OFF);
    }

    @Test
    void shutdownAllFansTurnsAllOff() {
        FanRepository fanRepository = Mockito.mock(FanRepository.class);
        RoomService roomService = Mockito.mock(RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Room room = new Room("Living Room");
        Fan fan1 = new Fan("Fan1", room);
        fan1.setId(1L);
        fan1.turnOn();
        Fan fan2 = new Fan("Fan2", room);
        fan2.setId(2L);
        fan2.turnOn();
        List<Fan> fans = List.of(fan1, fan2);

        Mockito.when(fanRepository.findAll()).thenReturn(fans);
        Mockito.when(fanRepository.save(Mockito.any(Fan.class))).thenAnswer(inv -> inv.getArgument(0));

        FanService svc = new FanService(fanRepository, roomService, messageSource);
        svc.shutdownAllFans();

        assertThat(fan1.getPowerState()).isEqualTo(PowerState.OFF);
        assertThat(fan2.getPowerState()).isEqualTo(PowerState.OFF);
    }

    @Test
    void deleteFanRemovesFan() {
        FanRepository fanRepository = Mockito.mock(FanRepository.class);
        RoomService roomService = Mockito.mock(RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Room room = new Room("Hall");
        Fan fan = new Fan("Ceiling Fan", room);
        Mockito.when(fanRepository.findById(8L)).thenReturn(Optional.of(fan));
        Mockito.doNothing().when(fanRepository).delete(fan);

        FanService svc = new FanService(fanRepository, roomService, messageSource);
        svc.deleteFan(8L);

        Mockito.verify(fanRepository).delete(fan);
        assertThat(room.getAppliances()).doesNotContain(fan);
    }
}
