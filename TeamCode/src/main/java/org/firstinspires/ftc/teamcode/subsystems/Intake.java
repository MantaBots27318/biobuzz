package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.command.SubsystemBase;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;

import org.firstinspires.ftc.teamcode.robot.BatteryVoltage;
import org.firstinspires.ftc.teamcode.robot.HardwareNames;

/**
 * Example mechanism: adapt it to the BIOBUZZ robot.
 * The template for every subsystem: settings at the top (editable live in Panels thanks to
 * @Configurable), a high-level API (collect / eject / stop), and no hardware access outside
 * this class.
 */
@Configurable
public class Intake extends SubsystemBase {
    public static double COLLECT_POWER = 1.0;
    public static double EJECT_POWER = -0.6;

    // MotorEx only sends a new power when it changed, which avoids useless writes to the hub
    private final MotorEx motor;
    private final BatteryVoltage battery;

    public Intake(HardwareMap hardwareMap, BatteryVoltage battery) {
        motor = new MotorEx(hardwareMap, HardwareNames.INTAKE);
        this.battery = battery;
    }

    public void collect() {
        motor.set(battery.compensate(COLLECT_POWER));
    }

    public void eject() {
        motor.set(battery.compensate(EJECT_POWER));
    }

    public void stop() {
        motor.set(0);
    }
}
