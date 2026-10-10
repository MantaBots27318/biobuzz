package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.util.TelemetryUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pre-match check: tests every motor, servo and continuous servo in the active configuration one by
 * one, without knowing anything about the robot. Run it in the pits after every repair.
 *
 * Gamepad 1: dpad left/right = previous/next device.
 * Motor or continuous servo: hold A = + direction, hold B = - direction.
 * Servo: Y = position 0.5, dpad up/down = +/- 0.05. A servo is only commanded after a press,
 * so a mechanism is never forced against its hard stop.
 */
@TeleOp(name = "System Check", group = "Test")
public class SystemCheck extends OpMode {
    private static final double MOTOR_TEST_POWER = 0.3;
    private static final double SERVO_STEP = 0.05;

    private final List<HardwareDevice> devices = new ArrayList<>();
    private final List<String> names = new ArrayList<>();
    // Position sent to each servo, tracked here because getPosition() means nothing until a position is sent
    private final Map<Servo, Double> servoTargets = new HashMap<>();
    private int index = 0;
    private boolean lastLeft, lastRight, lastUp, lastDown;

    @Override
    public void init() {
        telemetry = TelemetryUtil.withPanels(telemetry);
        add(DcMotorEx.class);
        add(CRServo.class);
        add(Servo.class);
    }

    private <T extends HardwareDevice> void add(Class<T> type) {
        for (T device : hardwareMap.getAll(type)) {
            devices.add(device);
            names.add(hardwareMap.getNamesOf(device).iterator().next());
        }
    }

    @Override
    public void loop() {
        if (devices.isEmpty()) {
            telemetry.addLine("No motor or servo in the active configuration");
            telemetry.update();
            return;
        }

        if (gamepad1.dpad_right && !lastRight) select(1);
        if (gamepad1.dpad_left && !lastLeft) select(-1);

        HardwareDevice device = devices.get(index);
        telemetry.addData("Device", "%d / %d: %s", index + 1, devices.size(), names.get(index));
        telemetry.addData("Connected to", device.getConnectionInfo());

        if (device instanceof DcMotorEx) {
            DcMotorEx motor = (DcMotorEx) device;
            motor.setPower(direction() * MOTOR_TEST_POWER);
            telemetry.addData("Type", "motor");
            telemetry.addData("Encoder", motor.getCurrentPosition());
            telemetry.addData("Current", "%.2f A", motor.getCurrent(CurrentUnit.AMPS));
        } else if (device instanceof CRServo) {
            ((CRServo) device).setPower(direction() * MOTOR_TEST_POWER);
            telemetry.addData("Type", "continuous servo");
        } else if (device instanceof Servo) {
            Servo servo = (Servo) device;
            Double target = servoTargets.get(servo);
            if (gamepad1.y) target = 0.5;
            if (gamepad1.dpad_up && !lastUp) target = Range.clip((target == null ? 0.5 : target) + SERVO_STEP, 0, 1);
            if (gamepad1.dpad_down && !lastDown) target = Range.clip((target == null ? 0.5 : target) - SERVO_STEP, 0, 1);
            if (target != null) {
                servo.setPosition(target);
                servoTargets.put(servo, target);
            }
            telemetry.addData("Type", "servo");
            telemetry.addData("Position", target == null ? "not commanded yet" : String.format("%.2f", target));
        }

        telemetry.addLine();
        telemetry.addLine("Dpad left/right: change device");
        telemetry.addLine("Hold A / B: spin (motor, continuous servo)");
        telemetry.addLine("Y, dpad up/down: move (servo)");
        telemetry.update();

        lastLeft = gamepad1.dpad_left;
        lastRight = gamepad1.dpad_right;
        lastUp = gamepad1.dpad_up;
        lastDown = gamepad1.dpad_down;
    }

    private void select(int step) {
        stopAll();
        index = (index + step + devices.size()) % devices.size();
    }

    private double direction() {
        return gamepad1.a ? 1 : gamepad1.b ? -1 : 0;
    }

    private void stopAll() {
        for (HardwareDevice device : devices) {
            if (device instanceof DcMotorEx) ((DcMotorEx) device).setPower(0);
            else if (device instanceof CRServo) ((CRServo) device).setPower(0);
        }
    }

    @Override
    public void stop() {
        stopAll();
    }
}
