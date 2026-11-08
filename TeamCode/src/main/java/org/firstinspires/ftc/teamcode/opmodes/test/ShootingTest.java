package org.firstinspires.ftc.teamcode.opmodes.test;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.robot.HardwareNames;
import org.firstinspires.ftc.teamcode.util.TelemetryUtil;

/**
 * Shooting test: spins the two shooter motors at the same speed in opposite directions, then stops
 * them by itself after RUN_TIME_S.
 *
 * Gamepad 1: A = shoot, B = same thing in the opposite direction.
 * If the ball goes the wrong way, swap the two motors' directions below (or the buttons).
 */
@Configurable
@TeleOp(name = "Shooting Test", group = "Test")
public class ShootingTest extends OpMode {
    public static double POWER = 1.5;
    public static double RUN_TIME_S = 0.2;

    private DcMotorEx leftMotor, rightMotor;
    private final ElapsedTime timer = new ElapsedTime();
    private double direction = 0; // 1 = shoot, -1 = reverse, 0 = stopped
    private boolean lastA, lastB;

    @Override
    public void init() {
        telemetry = TelemetryUtil.withPanels(telemetry);
        leftMotor = hardwareMap.get(DcMotorEx.class, HardwareNames.SHOOTER_LEFT);
        rightMotor = hardwareMap.get(DcMotorEx.class, HardwareNames.SHOOTER_RIGHT);
    }

    @Override
    public void loop() {
        // Only a new press starts a run, so holding a button does not restart it
        if (gamepad1.a && !lastA) start(1);
        if (gamepad1.b && !lastB) start(-1);
        lastA = gamepad1.a;
        lastB = gamepad1.b;

        if (direction != 0 && timer.seconds() >= RUN_TIME_S) {
            direction = 0;
        }

        // The motors face each other, so the same power in opposite directions
        leftMotor.setPower(direction * POWER);
        rightMotor.setPower(-direction * POWER);

        telemetry.addData("State", direction > 0 ? "shooting" : direction < 0 ? "reversing" : "stopped");
        telemetry.addData("Time left", "%.1f s", direction == 0 ? 0 : Math.max(0, RUN_TIME_S - timer.seconds()));
        telemetry.addLine("A: shoot   B: opposite direction");
        telemetry.update();
    }

    private void start(double newDirection) {
        direction = newDirection;
        timer.reset();
    }

    @Override
    public void stop() {
        leftMotor.setPower(0);
        rightMotor.setPower(0);
    }
}
