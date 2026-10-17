package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.command.SubsystemBase;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;

import org.firstinspires.ftc.teamcode.robot.BatteryVoltage;
import org.firstinspires.ftc.teamcode.robot.HardwareNames;

/**
 * Intake driven by a single motor, either running at COLLECT_POWER or stopped.
 * The encoder is not used yet.
 */
@Configurable
public class Intake extends SubsystemBase {
    // Negative to spin the other way. Editable live in Panels
    public static double COLLECT_POWER = 0.8;

    private final MotorEx motor;
    private final BatteryVoltage battery;
    private boolean running = false;

    public Intake(HardwareMap hardwareMap, BatteryVoltage battery) {
        motor = new MotorEx(hardwareMap, HardwareNames.INTAKE);
        this.battery = battery;
    }

    public void collect() {
        running = true;
    }

    public void stop() {
        running = false;
    }

    public void toggle() {
        running = !running;
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void periodic() {
        // Applied every loop so a COLLECT_POWER change in Panels takes effect immediately
        motor.set(running ? battery.compensate(COLLECT_POWER) : 0);
    }
}
