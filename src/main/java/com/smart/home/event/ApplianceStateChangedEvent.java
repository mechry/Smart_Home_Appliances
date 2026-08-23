package com.smart.home.event;

import com.smart.home.domain.Appliance;
import org.springframework.context.ApplicationEvent;

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

    public Long getApplianceId() {
        return applianceId;
    }

    public String getApplianceType() {
        return applianceType;
    }

    public String getNewState() {
        return newState;
    }
}
