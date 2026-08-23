package com.smart.home.event;

import com.smart.home.domain.Appliance;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ApplianceStateChangedEvent extends ApplicationEvent {

    private final Long applianceId;
    private final String applianceType;
    private final String newState;

    public ApplianceStateChangedEvent(Object source, Appliance appliance, String newState) {
        super(source);
        this.applianceId = appliance.getId();
        this.applianceType = appliance.getClass().getSimpleName();
        this.newState = newState;
    }
}
