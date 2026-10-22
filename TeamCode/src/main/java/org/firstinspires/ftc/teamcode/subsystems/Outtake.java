package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.command.SubsystemBase;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;

import org.firstinspires.ftc.teamcode.robot.HardwareNames;

/**
 * Outtake: two goBILDA 1150 RPM motors spinning in opposite directions, held at TARGET_RPM by a
 * velocity loop on their encoders, so the launch speed does not depend on the battery level.
 * The tuning procedure is in OuttakeTest.
 */
@Configurable
public class Outtake extends SubsystemBase {
    public static double TARGET_RPM = 800;
    public static double AT_SPEED_TOLERANCE_RPM = 30;

    // Velocity PID on the error in ticks/s, feedforward with KV = 1 meaning power = target / max speed
    public static double KP = 0;
    public static double KI = 0;
    public static double KD = 0;
    public static double KS = 0;
    public static double KV = 1.0;

    // The two motors face each other: one is reversed so the wheels spin in opposite directions
    public static boolean INVERT_LEFT = false;
    public static boolean INVERT_RIGHT = true;

    private final MotorEx left;
    private final MotorEx right;
    private boolean on = false;

    public Outtake(HardwareMap hardwareMap) {
        left = createMotor(hardwareMap, HardwareNames.OUTTAKE_LEFT);
        right = createMotor(hardwareMap, HardwareNames.OUTTAKE_RIGHT);
    }

    private static MotorEx createMotor(HardwareMap hardwareMap, String name) {
        MotorEx motor = new MotorEx(hardwareMap, name, Motor.GoBILDA.RPM_1150);
        // Coast down when stopped instead of braking the flywheels hard
        motor.setZeroPowerBehavior(Motor.ZeroPowerBehavior.FLOAT);
        motor.setRunMode(Motor.RunMode.RawPower);
        return motor;
    }

    public void spinUp() {
        if (on) return;
        left.setRunMode(Motor.RunMode.VelocityControl);
        right.setRunMode(Motor.RunMode.VelocityControl);
        on = true;
    }

    public void stop() {
        // Raw power 0 lets the wheels coast; velocity control at 0 RPM would brake them
        left.setRunMode(Motor.RunMode.RawPower);
        right.setRunMode(Motor.RunMode.RawPower);
        on = false;
    }

    public void toggle() {
        if (on) stop();
        else spinUp();
    }

    public boolean isOn() {
        return on;
    }

    public double targetRpm() {
        return on ? TARGET_RPM : 0;
    }

    public double leftRpm() {
        return toRpm(left, left.getCorrectedVelocity());
    }

    public double rightRpm() {
        return toRpm(right, right.getCorrectedVelocity());
    }

    public boolean atSpeed() {
        return on
                && Math.abs(leftRpm() - TARGET_RPM) < AT_SPEED_TOLERANCE_RPM
                && Math.abs(rightRpm() - TARGET_RPM) < AT_SPEED_TOLERANCE_RPM;
    }

    @Override
    public void periodic() {
        // Re-applied on every loop so that changes made in Panels take effect immediately
        update(left, INVERT_LEFT);
        update(right, INVERT_RIGHT);
    }

    private void update(MotorEx motor, boolean inverted) {
        // setInverted writes to the hub: only call it when the setting changed
        if (motor.getInverted() != inverted) motor.setInverted(inverted);
        motor.setVeloCoefficients(KP, KI, KD);
        motor.setFeedforwardCoefficients(KS, KV);
        // The velocity loop runs inside set(): it must be called on every loop, not once
        if (on) motor.setVelocity(TARGET_RPM / 60 * motor.getCPR());
        else motor.set(0);
    }

    private static double toRpm(MotorEx motor, double ticksPerSecond) {
        return ticksPerSecond * 60 / motor.getCPR();
    }
}
