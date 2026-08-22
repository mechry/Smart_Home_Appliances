package com.smart.home.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnnualMaintenanceService {

    private final LightService lightService;
    private final FanService fanService;
    private final AirConditionerService airConditionerService;

    @Transactional
    public void performAnnualShutdown() {
        log.info("Annual maintenance update started. Turning off all devices.");
        lightService.shutdownAllLights();
        fanService.shutdownAllFans();
        airConditionerService.shutdownAllAirConditioners();
        log.info("Annual maintenance update completed. All devices are off.");
    }
}
