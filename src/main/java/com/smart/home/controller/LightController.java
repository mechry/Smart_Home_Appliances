package com.smart.home.controller;

import com.smart.home.domain.Light;
import com.smart.home.dto.LightRequest;
import com.smart.home.service.LightService;
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
@RequestMapping("/api/lights")
@RequiredArgsConstructor
@Validated
public class LightController {

    private final LightService lightService;

    @PostMapping
    public ResponseEntity<Light> createLight(@Valid @RequestBody LightRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lightService.createLight(request));
    }

    @GetMapping
    public List<Light> getAllLights() {
        return lightService.getAllLights();
    }

    @GetMapping("/room/{roomId}")
    public List<Light> getLightsByRoom(@PathVariable Long roomId) {
        return lightService.getLightsByRoom(roomId);
    }

    @PutMapping("/rooms/{roomId}/all/on")
    public List<Light> turnOnAllByRoom(@PathVariable Long roomId) {
        return lightService.turnOnAllByRoom(roomId);
    }

    @PutMapping("/rooms/{roomId}/{lightId}/on")
    public Light turnOnByRoom(@PathVariable Long roomId, @PathVariable Long lightId) {
        return lightService.turnOnByRoom(roomId, lightId);
    }

    @PutMapping("/{lightId}/on")
    public Light turnOn(@PathVariable Long lightId) {
        return lightService.turnOn(lightId);
    }

    @PutMapping("/rooms/{roomId}/all/off")
    public List<Light> turnOffAllByRoom(@PathVariable Long roomId) {
        return lightService.turnOffAllByRoom(roomId);
    }

    @PutMapping("/rooms/{roomId}/{lightId}/off")
    public Light turnOffByRoom(@PathVariable Long roomId, @PathVariable Long lightId) {
        return lightService.turnOffByRoom(roomId, lightId);
    }

    @PutMapping("/{lightId}/off")
    public Light turnOff(@PathVariable Long lightId) {
        return lightService.turnOff(lightId);
    }

    @DeleteMapping("/{lightId}")
    public ResponseEntity<Void> deleteLight(@PathVariable Long lightId) {
        lightService.deleteLight(lightId);
        return ResponseEntity.noContent().build();
    }
}
