package org.firstinspires.ftc.teamcode.util;

import com.qualcomm.robotcore.util.ElapsedTime;

/** Measures the loop rate. Show it in telemetry to spot any slowdown. */
public class LoopTimer {
    private final ElapsedTime timer = new ElapsedTime();
    private double lastLoopMs;

    /** Call once per loop. */
    public void tick() {
        lastLoopMs = timer.milliseconds();
        timer.reset();
    }

    public double hz() {
        return lastLoopMs > 0 ? 1000.0 / lastLoopMs : 0;
    }

    public double ms() {
        return lastLoopMs;
    }
}
