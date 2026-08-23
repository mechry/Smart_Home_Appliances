package com.smart.home.controller;

import com.smart.home.domain.AirConditioner;
import com.smart.home.dto.AirConditionerRequest;
import com.smart.home.service.AirConditionerService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/air-conditioners")
@RequiredArgsConstructor
@Validated
public class AirConditionerController {

    private final AirConditionerService airConditionerService;

    @PostMapping
    public ResponseEntity<AirConditioner> createAirConditioner(@Valid @RequestBody AirConditionerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(airConditionerService.createAirConditioner(request));
    }

    @GetMapping
    public List<AirConditioner> getAllAirConditioners() {
        return airConditionerService.getAllAirConditioners();
    }

    @GetMapping("/room/{roomId}")
    public List<AirConditioner> getAirConditionersByRoom(@PathVariable Long roomId) {
        return airConditionerService.getAirConditionersByRoom(roomId);
    }

    @PutMapping("/rooms/{roomId}/all/on")
    public List<AirConditioner> turnOnAllByRoom(@PathVariable Long roomId) {
        return airConditionerService.turnOnAllByRoom(roomId);
    }

    @PutMapping("/rooms/{roomId}/{airConditionerId}/on")
    public AirConditioner turnOnByRoom(@PathVariable Long roomId, @PathVariable Long airConditionerId) {
        return airConditionerService.turnOnByRoom(roomId, airConditionerId);
    }

    @PutMapping("/{airConditionerId}/on")
    public AirConditioner turnOn(@PathVariable Long airConditionerId) {
        return airConditionerService.turnOn(airConditionerId);
    }

    @PutMapping("/rooms/{roomId}/all/off")
    public List<AirConditioner> turnOffAllByRoom(@PathVariable Long roomId) {
        return airConditionerService.turnOffAllByRoom(roomId);
    }

    @PutMapping("/rooms/{roomId}/{airConditionerId}/off")
    public AirConditioner turnOffByRoom(@PathVariable Long roomId, @PathVariable Long airConditionerId) {
        return airConditionerService.turnOffByRoom(roomId, airConditionerId);
    }

    @PutMapping("/{airConditionerId}/off")
    public AirConditioner turnOff(@PathVariable Long airConditionerId) {
        return airConditionerService.turnOff(airConditionerId);
    }

    @DeleteMapping("/{airConditionerId}")
    public ResponseEntity<Void> deleteAirConditioner(@PathVariable Long airConditionerId) {
        airConditionerService.deleteAirConditioner(airConditionerId);
        return ResponseEntity.noContent().build();
    }
}
