package com.smart.home.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("FAN")
@Getter
@Setter
@NoArgsConstructor
public class Fan extends Appliance {

    @Min(value = 0, message = "{fan.speed.min}")
    @Max(value = 2, message = "{fan.speed.max}")
    @Column(name = "speed")
    private int speed = 0;

    public Fan(String name, Room room) {
        super(name, room);
    }

    @Override
    public void turnOn() {
        if (speed == 0) {
            speed = 1;
        }
        setPowerState(PowerState.ON);
    }

    @Override
    public void turnOff() {
        this.speed = 0;
        setPowerState(PowerState.OFF);
    }
}
