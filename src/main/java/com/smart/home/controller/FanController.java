package com.smart.home.controller;

import com.smart.home.domain.Fan;
import com.smart.home.dto.FanRequest;
import com.smart.home.dto.FanSpeedRequest;
import com.smart.home.service.FanService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fans")
@RequiredArgsConstructor
@Validated
public class FanController {

    private final FanService fanService;

    @PostMapping
    public ResponseEntity<Fan> createFan(@Valid @RequestBody FanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fanService.createFan(request));
    }

    @GetMapping
    public List<Fan> getAllFans() {
        return fanService.getAllFans();
    }

    @GetMapping("/room/{roomId}")
    public List<Fan> getFansByRoom(@PathVariable Long roomId) {
        return fanService.getFansByRoom(roomId);
    }

    @PutMapping("/{fanId}/speed")
    public Fan updateFanSpeed(@PathVariable Long fanId, @Valid @RequestBody FanSpeedRequest request) {
        return fanService.updateFanSpeed(fanId, request.speed());
    }

    @PutMapping("/rooms/{roomId}/{fanId}/speed")
    public Fan updateFanSpeedByRoom(@PathVariable Long roomId, @PathVariable Long fanId, @Valid @RequestBody FanSpeedRequest request) {
        return fanService.updateFanSpeedByRoom(roomId, fanId, request.speed());
    }

    @PutMapping("/rooms/{roomId}/all/on")
    public List<Fan> turnOnAllByRoom(@PathVariable Long roomId) {
        return fanService.turnOnAllByRoom(roomId);
    }

    @PutMapping("/rooms/{roomId}/{fanId}/on")
    public Fan turnOnByRoom(@PathVariable Long roomId, @PathVariable Long fanId) {
        return fanService.turnOnByRoom(roomId, fanId);
    }

    @PutMapping("/{fanId}/on")
    public Fan turnOn(@PathVariable Long fanId) {
        return fanService.turnOn(fanId);
    }

    @PutMapping("/rooms/{roomId}/all/off")
    public List<Fan> turnOffAllByRoom(@PathVariable Long roomId) {
        return fanService.turnOffAllByRoom(roomId);
    }

    @PutMapping("/rooms/{roomId}/{fanId}/off")
    public Fan turnOffByRoom(@PathVariable Long roomId, @PathVariable Long fanId) {
        return fanService.turnOffByRoom(roomId, fanId);
    }

    @PutMapping("/{fanId}/off")
    public Fan turnOff(@PathVariable Long fanId) {
        return fanService.turnOff(fanId);
    }
}
