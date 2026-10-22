package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.command.SubsystemBase;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;

import org.firstinspires.ftc.teamcode.robot.BatteryVoltage;
import org.firstinspires.ftc.teamcode.robot.HardwareNames;

/**
 * Intake driven by a single motor, running forward or in reverse at COLLECT_POWER, or stopped.
 * The encoder is not used yet.
 */
@Configurable
public class Intake extends SubsystemBase {
    // Editable live in Panels
    public static double COLLECT_POWER = 0.8;

    private final MotorEx motor;
    private final BatteryVoltage battery;
    private enum Direction { STOPPED, FORWARD, REVERSE }

    private Direction direction = Direction.STOPPED;

    public Intake(HardwareMap hardwareMap, BatteryVoltage battery) {
        motor = new MotorEx(hardwareMap, HardwareNames.INTAKE);
        // The motor is mounted so that positive power spins the wrong way for "forward"
        motor.setInverted(true);
        this.battery = battery;
    }

    public void toggleForward() {
        direction = (direction == Direction.FORWARD) ? Direction.STOPPED : Direction.FORWARD;
    }

    public void toggleReverse() {
        direction = (direction == Direction.REVERSE) ? Direction.STOPPED : Direction.REVERSE;
    }

    public void stop() {
        direction = Direction.STOPPED;
    }

    public boolean isRunning() {
        return direction != Direction.STOPPED;
    }

    public String directionName() {
        return direction.name().toLowerCase();
    }

    @Override
    public void periodic() {
        // Applied every loop so a COLLECT_POWER change in Panels takes effect immediately
        double power = 0;
        if (direction == Direction.FORWARD) power = COLLECT_POWER;
        else if (direction == Direction.REVERSE) power = -COLLECT_POWER;
        motor.set(battery.compensate(power));
    }
}
