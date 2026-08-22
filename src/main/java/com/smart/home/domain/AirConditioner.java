package com.smart.home.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("AIR_CONDITIONER")
@Getter
@Setter
@NoArgsConstructor
public class AirConditioner extends Appliance {

    @Enumerated(EnumType.STRING)
    @Column(name = "thermostat_mode")
    private ThermostatMode thermostatMode = ThermostatMode.OFF;

    public AirConditioner(String name, Room room) {
        super(name, room);
    }

    @Override
    public void turnOn() {
        this.thermostatMode = ThermostatMode.COOL;
        setPowerState(PowerState.ON);
    }

    @Override
    public void turnOff() {
        this.thermostatMode = ThermostatMode.OFF;
        setPowerState(PowerState.OFF);
    }

    public enum ThermostatMode {
        OFF,
        COOL,
        HEAT,
        DRY
    }
}
