package org.firstinspires.ftc.teamcode.robot;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

/**
 * Battery voltage, re-read at most every REFRESH_MS: each read is a request to the hub
 * that slows the loop down.
 */
@Configurable
public class BatteryVoltage {
    public static double NOMINAL_VOLTS = 12.0;
    public static double REFRESH_MS = 500;

    private final VoltageSensor sensor;
    private final ElapsedTime sinceRead = new ElapsedTime();
    private double volts = NOMINAL_VOLTS;

    public BatteryVoltage(HardwareMap hardwareMap) {
        sensor = hardwareMap.voltageSensor.iterator().next();
        read();
    }

    public double volts() {
        if (sinceRead.milliseconds() > REFRESH_MS) read();
        return volts;
    }

    /** Power to send to get the same effect as with a battery at NOMINAL_VOLTS. */
    public double compensate(double power) {
        return Range.clip(power * NOMINAL_VOLTS / volts(), -1, 1);
    }

    private void read() {
        double v = sensor.getVoltage();
        // A failed read returns 0: keep the last valid value
        if (v > 0) volts = v;
        sinceRead.reset();
    }
}
