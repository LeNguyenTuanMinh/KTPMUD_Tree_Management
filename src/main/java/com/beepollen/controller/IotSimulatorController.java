package com.beepollen.controller;

import com.beepollen.iot.IotSimulatorState;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/iot-simulator")
@RequiredArgsConstructor
public class IotSimulatorController {

    private final IotSimulatorState iotSimulatorState;

    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'BEEKEEPER')")
    public Map<String, Boolean> getStatus() {
        return Map.of("paused", iotSimulatorState.isPaused());
    }

    @PostMapping("/toggle")
    @PreAuthorize("hasAnyRole('ADMIN', 'BEEKEEPER')")
    public Map<String, Boolean> toggleSimulator() {
        iotSimulatorState.toggle();
        return Map.of("paused", iotSimulatorState.isPaused());
    }
}
