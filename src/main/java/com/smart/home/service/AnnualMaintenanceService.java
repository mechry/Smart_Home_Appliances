package com.smart.home.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnnualMaintenanceService {

    private final List<ApplianceManager> applianceManagers;

    @Transactional
    public void performAnnualShutdown() {
        log.info("Annual maintenance update started. Turning off all devices.");
        for (ApplianceManager manager : applianceManagers) {
            manager.shutdownAll();
        }
        log.info("Annual maintenance update completed. All devices are off.");
    }
}
