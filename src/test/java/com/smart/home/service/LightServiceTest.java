package com.smart.home;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.smart.home.domain.Light;
import com.smart.home.domain.PowerState;
import com.smart.home.domain.Room;
import com.smart.home.dto.LightRequest;
import com.smart.home.exception.ResourceNotFoundException;
import com.smart.home.repository.LightRepository;
import com.smart.home.service.LightService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;

class LightServiceTest {

    @Test
    void createLightSavesLight() {
        LightRepository lightRepository = Mockito.mock(LightRepository.class);
        com.smart.home.service.RoomService roomService = Mockito.mock(com.smart.home.service.RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Room room = new Room("Living Room");
        LightRequest request = new LightRequest("Lamp", 1L);
        Light saved = new Light("Lamp", room);

        Mockito.when(roomService.getRoomById(1L)).thenReturn(room);
        Mockito.when(lightRepository.save(Mockito.any(Light.class))).thenReturn(saved);

        LightService svc = new LightService(lightRepository, roomService, messageSource);
        Light result = svc.createLight(request);

        assertThat(result.getName()).isEqualTo("Lamp");
    }

    @Test
    void getAllLightsReturnsAll() {
        LightRepository lightRepository = Mockito.mock(LightRepository.class);
        com.smart.home.service.RoomService roomService = Mockito.mock(com.smart.home.service.RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        List<Light> lights = List.of(new Light("A", new Room("R1")), new Light("B", new Room("R2")));
        Mockito.when(lightRepository.findAll()).thenReturn(lights);

        LightService svc = new LightService(lightRepository, roomService, messageSource);
        assertThat(svc.getAllLights()).hasSize(2).extracting(Light::getName).containsExactly("A", "B");
    }

    @Test
    void getLightsByRoomReturnsList() {
        LightRepository lightRepository = Mockito.mock(LightRepository.class);
        com.smart.home.service.RoomService roomService = Mockito.mock(com.smart.home.service.RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Room room = new Room("Kitchen");
        List<Light> lights = List.of(new Light("L1", room));

        Mockito.when(roomService.getRoomById(5L)).thenReturn(room);
        Mockito.when(lightRepository.findByRoomId(5L)).thenReturn(lights);

        LightService svc = new LightService(lightRepository, roomService, messageSource);
        assertThat(svc.getLightsByRoom(5L)).hasSize(1).extracting(Light::getName).containsExactly("L1");
    }

    @Test
    void getLightByIdReturnsExistingLight() {
        LightRepository lightRepository = Mockito.mock(LightRepository.class);
        com.smart.home.service.RoomService roomService = Mockito.mock(com.smart.home.service.RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Light light = new Light("Desk", new Room("Office"));
        Mockito.when(lightRepository.findById(3L)).thenReturn(Optional.of(light));

        LightService svc = new LightService(lightRepository, roomService, messageSource);
        assertThat(svc.getLight(3L)).isEqualTo(light);
    }

    @Test
    void getLightByIdThrowsNotFound() {
        LightRepository lightRepository = Mockito.mock(LightRepository.class);
        com.smart.home.service.RoomService roomService = Mockito.mock(com.smart.home.service.RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Mockito.when(lightRepository.findById(999L)).thenReturn(Optional.empty());
        Mockito.when(messageSource.getMessage(Mockito.eq("light.not.found"), Mockito.any(Object[].class), Mockito.eq("light.not.found"), Mockito.any()))
                .thenReturn("Light not found with id 999");

        LightService svc = new LightService(lightRepository, roomService, messageSource);

        assertThatThrownBy(() -> svc.getLight(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Light not found with id 999");
    }

    @Test
    void turnOnTurnsLightOn() {
        LightRepository lightRepository = Mockito.mock(LightRepository.class);
        com.smart.home.service.RoomService roomService = Mockito.mock(com.smart.home.service.RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Room room = new Room("Hall");
        Light light = new Light("Chandelier", room);
        Mockito.when(lightRepository.findById(7L)).thenReturn(Optional.of(light));
        Mockito.when(lightRepository.save(Mockito.any(Light.class))).thenAnswer(inv -> inv.getArgument(0));

        LightService svc = new LightService(lightRepository, roomService, messageSource);
        Light res = svc.turnOn(7L);

        assertThat(res.getPowerState()).isEqualTo(PowerState.ON);
        assertThat(res.getSwitchPosition()).isEqualTo(Light.SwitchPosition.ON);
    }

    @Test
    void turnOffTurnsLightOff() {
        LightRepository lightRepository = Mockito.mock(LightRepository.class);
        com.smart.home.service.RoomService roomService = Mockito.mock(com.smart.home.service.RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Room room = new Room("Hall");
        Light light = new Light("Chandelier", room);
        light.turnOn();

        Mockito.when(lightRepository.findById(8L)).thenReturn(Optional.of(light));
        Mockito.when(lightRepository.save(Mockito.any(Light.class))).thenAnswer(inv -> inv.getArgument(0));

        LightService svc = new LightService(lightRepository, roomService, messageSource);
        Light res = svc.turnOff(8L);

        assertThat(res.getPowerState()).isEqualTo(PowerState.OFF);
        assertThat(res.getSwitchPosition()).isEqualTo(Light.SwitchPosition.OFF);
    }

    @Test
    void deleteLightRemovesLight() {
        LightRepository lightRepository = Mockito.mock(LightRepository.class);
        com.smart.home.service.RoomService roomService = Mockito.mock(com.smart.home.service.RoomService.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        Room room = new Room("Hall");
        Light light = new Light("Chandelier", room);
        Mockito.when(lightRepository.findById(8L)).thenReturn(Optional.of(light));
        Mockito.doNothing().when(lightRepository).delete(light);

        LightService svc = new LightService(lightRepository, roomService, messageSource);
        svc.deleteLight(8L);

        Mockito.verify(lightRepository).delete(light);
        assertThat(room.getAppliances()).doesNotContain(light);
    }
}
