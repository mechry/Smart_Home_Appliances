package com.smart.home.scheduler;

import com.smart.home.service.AnnualMaintenanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class YearlyMaintenanceScheduler {

    private final AnnualMaintenanceService annualMaintenanceService;

    @Scheduled(cron = "0 0 1 1 * *", zone = "${app.maintenance.timezone:America/New_York}")
    public void performAnnualUpdate() {
        log.info("Scheduled annual maintenance trigger fired.");
        annualMaintenanceService.performAnnualShutdown();
    }
}
