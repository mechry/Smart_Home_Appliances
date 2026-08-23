package com.smart.home.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.smart.home.domain.AirConditioner;
import com.smart.home.domain.PowerState;
import com.smart.home.domain.Room;
import com.smart.home.dto.AirConditionerRequest;
import com.smart.home.exception.ResourceNotFoundException;
import com.smart.home.repository.AirConditionerRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;

class AirConditionerServiceTest {

    @Test
    void createAirConditionerSavesAirConditioner() {
        AirConditionerRepository airConditionerRepository = Mockito.mock(AirConditionerRepository.class);
        RoomService roomService = Mockito.mock(RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Room room = new Room("Living Room");
        AirConditionerRequest request = new AirConditionerRequest("AC Unit", 1L);
        AirConditioner saved = new AirConditioner("AC Unit", room);

        Mockito.when(roomService.getRoomById(1L)).thenReturn(room);
        Mockito.when(airConditionerRepository.save(Mockito.any(AirConditioner.class))).thenReturn(saved);

        AirConditionerService svc = new AirConditionerService(airConditionerRepository, roomService, messageSource);
        AirConditioner result = svc.createAirConditioner(request);

        assertThat(result.getName()).isEqualTo("AC Unit");
    }

    @Test
    void getAllAirConditionersReturnsAll() {
        AirConditionerRepository airConditionerRepository = Mockito.mock(AirConditionerRepository.class);
        RoomService roomService = Mockito.mock(RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        List<AirConditioner> airConditioners = List.of(
                new AirConditioner("AC1", new Room("R1")),
                new AirConditioner("AC2", new Room("R2"))
        );
        Mockito.when(airConditionerRepository.findAll()).thenReturn(airConditioners);

        AirConditionerService svc = new AirConditionerService(airConditionerRepository, roomService, messageSource);
        assertThat(svc.getAllAirConditioners()).hasSize(2)
                .extracting(AirConditioner::getName)
                .containsExactly("AC1", "AC2");
    }

    @Test
    void getAirConditionersByRoomReturnsList() {
        AirConditionerRepository airConditionerRepository = Mockito.mock(AirConditionerRepository.class);
        RoomService roomService = Mockito.mock(RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Room room = new Room("Bedroom");
        List<AirConditioner> airConditioners = List.of(new AirConditioner("Bedroom AC", room));

        Mockito.when(roomService.getRoomById(5L)).thenReturn(room);
        Mockito.when(airConditionerRepository.findByRoomId(5L)).thenReturn(airConditioners);

        AirConditionerService svc = new AirConditionerService(airConditionerRepository, roomService, messageSource);
        assertThat(svc.getAirConditionersByRoom(5L)).hasSize(1)
                .extracting(AirConditioner::getName)
                .containsExactly("Bedroom AC");
    }

    @Test
    void getAirConditionerByIdReturnsExistingAirConditioner() {
        AirConditionerRepository airConditionerRepository = Mockito.mock(AirConditionerRepository.class);
        RoomService roomService = Mockito.mock(RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        AirConditioner airConditioner = new AirConditioner("Office AC", new Room("Office"));
        Mockito.when(airConditionerRepository.findById(3L)).thenReturn(Optional.of(airConditioner));

        AirConditionerService svc = new AirConditionerService(airConditionerRepository, roomService, messageSource);
        assertThat(svc.getAirConditioner(3L)).isEqualTo(airConditioner);
    }

    @Test
    void getAirConditionerByIdThrowsNotFound() {
        AirConditionerRepository airConditionerRepository = Mockito.mock(AirConditionerRepository.class);
        RoomService roomService = Mockito.mock(RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Mockito.when(airConditionerRepository.findById(999L)).thenReturn(Optional.empty());
        Mockito.when(messageSource.getMessage(
                Mockito.eq("air.conditioner.not.found"),
                Mockito.any(Object[].class),
                Mockito.eq("air.conditioner.not.found"),
                Mockito.any()
        )).thenReturn("Air conditioner not found with id 999");

        AirConditionerService svc = new AirConditionerService(airConditionerRepository, roomService, messageSource);

        assertThatThrownBy(() -> svc.getAirConditioner(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Air conditioner not found with id 999");
    }

    @Test
    void turnOnTurnsAirConditionerOn() {
        AirConditionerRepository airConditionerRepository = Mockito.mock(AirConditionerRepository.class);
        RoomService roomService = Mockito.mock(RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Room room = new Room("Hall");
        AirConditioner airConditioner = new AirConditioner("Hall AC", room);
        Mockito.when(airConditionerRepository.findById(7L)).thenReturn(Optional.of(airConditioner));
        Mockito.when(airConditionerRepository.save(Mockito.any(AirConditioner.class))).thenAnswer(inv -> inv.getArgument(0));

        AirConditionerService svc = new AirConditionerService(airConditionerRepository, roomService, messageSource);
        AirConditioner res = svc.turnOn(7L);

        assertThat(res.getPowerState()).isEqualTo(PowerState.ON);
        assertThat(res.getThermostatMode()).isEqualTo(AirConditioner.ThermostatMode.COOL);
    }

    @Test
    void turnOffTurnsAirConditionerOff() {
        AirConditionerRepository airConditionerRepository = Mockito.mock(AirConditionerRepository.class);
        RoomService roomService = Mockito.mock(RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Room room = new Room("Hall");
        AirConditioner airConditioner = new AirConditioner("Hall AC", room);
        airConditioner.turnOn();

        Mockito.when(airConditionerRepository.findById(8L)).thenReturn(Optional.of(airConditioner));
        Mockito.when(airConditionerRepository.save(Mockito.any(AirConditioner.class))).thenAnswer(inv -> inv.getArgument(0));

        AirConditionerService svc = new AirConditionerService(airConditionerRepository, roomService, messageSource);
        AirConditioner res = svc.turnOff(8L);

        assertThat(res.getPowerState()).isEqualTo(PowerState.OFF);
        assertThat(res.getThermostatMode()).isEqualTo(AirConditioner.ThermostatMode.OFF);
    }

    @Test
    void turnOnByRoomTurnsAirConditionerOn() {
        AirConditionerRepository airConditionerRepository = Mockito.mock(AirConditionerRepository.class);
        RoomService roomService = Mockito.mock(RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Room room = new Room("Kitchen");
        AirConditioner airConditioner = new AirConditioner("Kitchen AC", room);
        airConditioner.setId(10L);

        Mockito.when(roomService.getRoomById(5L)).thenReturn(room);
        Mockito.when(airConditionerRepository.findById(10L)).thenReturn(Optional.of(airConditioner));
        Mockito.when(airConditionerRepository.save(Mockito.any(AirConditioner.class))).thenAnswer(inv -> inv.getArgument(0));

        AirConditionerService svc = new AirConditionerService(airConditionerRepository, roomService, messageSource);
        AirConditioner res = svc.turnOnByRoom(5L, 10L);

        assertThat(res.getPowerState()).isEqualTo(PowerState.ON);
        assertThat(res.getThermostatMode()).isEqualTo(AirConditioner.ThermostatMode.COOL);
    }

    @Test
    void turnOnAllByRoomTurnsAllOn() {
        AirConditionerRepository airConditionerRepository = Mockito.mock(AirConditionerRepository.class);
        RoomService roomService = Mockito.mock(RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Room room = new Room("Living Room");
        AirConditioner ac1 = new AirConditioner("AC1", room);
        ac1.setId(1L);
        AirConditioner ac2 = new AirConditioner("AC2", room);
        ac2.setId(2L);
        List<AirConditioner> airConditioners = List.of(ac1, ac2);

        Mockito.when(roomService.getRoomById(3L)).thenReturn(room);
        Mockito.when(airConditionerRepository.findByRoomId(3L)).thenReturn(airConditioners);
        Mockito.when(airConditionerRepository.save(Mockito.any(AirConditioner.class))).thenAnswer(inv -> inv.getArgument(0));

        AirConditionerService svc = new AirConditionerService(airConditionerRepository, roomService, messageSource);
        List<AirConditioner> result = svc.turnOnAllByRoom(3L);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(ac -> ac.getPowerState() == PowerState.ON);
    }

    @Test
    void turnOffByRoomTurnsAirConditionerOff() {
        AirConditionerRepository airConditionerRepository = Mockito.mock(AirConditionerRepository.class);
        RoomService roomService = Mockito.mock(RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Room room = new Room("Kitchen");
        AirConditioner airConditioner = new AirConditioner("Kitchen AC", room);
        airConditioner.setId(10L);
        airConditioner.turnOn();

        Mockito.when(roomService.getRoomById(5L)).thenReturn(room);
        Mockito.when(airConditionerRepository.findById(10L)).thenReturn(Optional.of(airConditioner));
        Mockito.when(airConditionerRepository.save(Mockito.any(AirConditioner.class))).thenAnswer(inv -> inv.getArgument(0));

        AirConditionerService svc = new AirConditionerService(airConditionerRepository, roomService, messageSource);
        AirConditioner res = svc.turnOffByRoom(5L, 10L);

        assertThat(res.getPowerState()).isEqualTo(PowerState.OFF);
        assertThat(res.getThermostatMode()).isEqualTo(AirConditioner.ThermostatMode.OFF);
    }

    @Test
    void turnOffAllByRoomTurnsAllOff() {
        AirConditionerRepository airConditionerRepository = Mockito.mock(AirConditionerRepository.class);
        RoomService roomService = Mockito.mock(RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Room room = new Room("Living Room");
        AirConditioner ac1 = new AirConditioner("AC1", room);
        ac1.setId(1L);
        ac1.turnOn();
        AirConditioner ac2 = new AirConditioner("AC2", room);
        ac2.setId(2L);
        ac2.turnOn();
        List<AirConditioner> airConditioners = List.of(ac1, ac2);

        Mockito.when(roomService.getRoomById(3L)).thenReturn(room);
        Mockito.when(airConditionerRepository.findByRoomId(3L)).thenReturn(airConditioners);
        Mockito.when(airConditionerRepository.save(Mockito.any(AirConditioner.class))).thenAnswer(inv -> inv.getArgument(0));

        AirConditionerService svc = new AirConditionerService(airConditionerRepository, roomService, messageSource);
        List<AirConditioner> result = svc.turnOffAllByRoom(3L);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(ac -> ac.getPowerState() == PowerState.OFF);
    }

    @Test
    void shutdownAllAirConditionersTurnsAllOff() {
        AirConditionerRepository airConditionerRepository = Mockito.mock(AirConditionerRepository.class);
        RoomService roomService = Mockito.mock(RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Room room = new Room("Living Room");
        AirConditioner ac1 = new AirConditioner("AC1", room);
        ac1.setId(1L);
        ac1.turnOn();
        AirConditioner ac2 = new AirConditioner("AC2", room);
        ac2.setId(2L);
        ac2.turnOn();
        List<AirConditioner> airConditioners = List.of(ac1, ac2);

        Mockito.when(airConditionerRepository.findAll()).thenReturn(airConditioners);
        Mockito.when(airConditionerRepository.save(Mockito.any(AirConditioner.class))).thenAnswer(inv -> inv.getArgument(0));

        AirConditionerService svc = new AirConditionerService(airConditionerRepository, roomService, messageSource);
        svc.shutdownAllAirConditioners();

        assertThat(ac1.getPowerState()).isEqualTo(PowerState.OFF);
        assertThat(ac2.getPowerState()).isEqualTo(PowerState.OFF);
    }

    @Test
    void deleteAirConditionerRemovesAirConditioner() {
        AirConditionerRepository airConditionerRepository = Mockito.mock(AirConditionerRepository.class);
        RoomService roomService = Mockito.mock(RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Room room = new Room("Hall");
        AirConditioner airConditioner = new AirConditioner("Hall AC", room);
        Mockito.when(airConditionerRepository.findById(8L)).thenReturn(Optional.of(airConditioner));
        Mockito.doNothing().when(airConditionerRepository).delete(airConditioner);

        AirConditionerService svc = new AirConditionerService(airConditionerRepository, roomService, messageSource);
        svc.deleteAirConditioner(8L);

        Mockito.verify(airConditionerRepository).delete(airConditioner);
        assertThat(room.getAppliances()).doesNotContain(airConditioner);
    }
}
