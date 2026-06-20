package com.beepollen.iot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Holds the runtime state of the IoT Simulator.
 */
@Component
public class IotSimulatorState {

    private final AtomicBoolean paused;

    public IotSimulatorState(@Value("${iot.simulator.enabled:true}") boolean enabled) {
        // If enabled by default, it is NOT paused.
        this.paused = new AtomicBoolean(!enabled);
    }

    public boolean isPaused() {
        return paused.get();
    }

    public boolean isRunning() {
        return !paused.get();
    }

    public void pause() {
        paused.set(true);
    }

    public void resume() {
        paused.set(false);
    }

    public void toggle() {
        paused.set(!paused.get());
    }
}
