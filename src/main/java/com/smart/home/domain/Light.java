package com.smart.home.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("LIGHT")
@Getter
@Setter
@NoArgsConstructor
public class Light extends Appliance {

    @Enumerated(EnumType.STRING)
    @Column(name = "switch_position")
    private SwitchPosition switchPosition = SwitchPosition.OFF;

    public Light(String name, Room room) {
        super(name, room);
    }

    @Override
    public void turnOn() {
        this.switchPosition = SwitchPosition.ON;
        setPowerState(PowerState.ON);
    }

    @Override
    public void turnOff() {
        this.switchPosition = SwitchPosition.OFF;
        setPowerState(PowerState.OFF);
    }

    public enum SwitchPosition {
        ON,
        OFF
    }
}
