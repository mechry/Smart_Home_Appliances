package com.smart.home;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.smart.home.domain.AirConditioner;
import com.smart.home.domain.Fan;
import com.smart.home.domain.Light;
import com.smart.home.domain.PowerState;
import com.smart.home.domain.Room;
import com.smart.home.exception.ResourceNotFoundException;
import com.smart.home.repository.RoomRepository;
import com.smart.home.service.RoomService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;

class SmartHomeApplicationTests {

    @Test
    void lightCanBeTurnedOnAndOff() {
        Room room = new Room("Living Room");
        Light light = new Light("Lamp", room);

        light.turnOn();
        assertThat(light.getPowerState()).isEqualTo(PowerState.ON);
        assertThat(light.getSwitchPosition()).isEqualTo(Light.SwitchPosition.ON);

        light.turnOff();
        assertThat(light.getPowerState()).isEqualTo(PowerState.OFF);
        assertThat(light.getSwitchPosition()).isEqualTo(Light.SwitchPosition.OFF);
    }

    @Test
    void fanStopsAtZeroSpeed() {
        Room room = new Room("Bedroom");
        Fan fan = new Fan("Ceiling Fan", room);

        fan.turnOn();
        assertThat(fan.getSpeed()).isEqualTo(1);
        assertThat(fan.getPowerState()).isEqualTo(PowerState.ON);

        fan.turnOff();
        assertThat(fan.getSpeed()).isZero();
        assertThat(fan.getPowerState()).isEqualTo(PowerState.OFF);
    }

    @Test
    void airConditionerTurnsOffUsingThermostatMode() {
        Room room = new Room("Kitchen");
        AirConditioner airConditioner = new AirConditioner("AC", room);

        airConditioner.turnOn();
        assertThat(airConditioner.getPowerState()).isEqualTo(PowerState.ON);
        assertThat(airConditioner.getThermostatMode()).isEqualTo(AirConditioner.ThermostatMode.COOL);

        airConditioner.turnOff();
        assertThat(airConditioner.getPowerState()).isEqualTo(PowerState.OFF);
        assertThat(airConditioner.getThermostatMode()).isEqualTo(AirConditioner.ThermostatMode.OFF);
    }

    @Test
    void roomCanContainMultipleLights() {
        Room room = new Room("Living Room");
        Light primaryLight = new Light("Main Light", room);
        Light accentLight = new Light("Accent Light", room);

        room.addAppliance(primaryLight);
        room.addAppliance(accentLight);

        assertThat(room.getAppliances()).hasSize(2);
        assertThat(room.getAppliances()).extracting("name").containsExactlyInAnyOrder("Main Light", "Accent Light");
    }

    @Test
    void roomCanContainMultipleFans() {
        Room room = new Room("Bedroom");
        Fan ceilingFan = new Fan("Ceiling Fan", room);
        Fan deskFan = new Fan("Desk Fan", room);

        room.addAppliance(ceilingFan);
        room.addAppliance(deskFan);

        assertThat(room.getAppliances()).hasSize(2);
        assertThat(room.getAppliances()).extracting("name").containsExactlyInAnyOrder("Ceiling Fan", "Desk Fan");
    }

    @Test
    void roomCanContainMultipleAirConditioners() {
        Room room = new Room("Office");
        AirConditioner mainAc = new AirConditioner("Main AC", room);
        AirConditioner secondaryAc = new AirConditioner("Secondary AC", room);

        room.addAppliance(mainAc);
        room.addAppliance(secondaryAc);

        assertThat(room.getAppliances()).hasSize(2);
        assertThat(room.getAppliances()).extracting("name").containsExactlyInAnyOrder("Main AC", "Secondary AC");
    }

    @Test
    void applianceCanOnlyBelongToOneRoom() {
        Room livingRoom = new Room("Living Room");
        Room bedroom = new Room("Bedroom");
        Light light = new Light("Lamp", livingRoom);

        livingRoom.addAppliance(light);

        assertThatThrownBy(() -> bedroom.addAppliance(light))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("{appliance.reassign}");
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
