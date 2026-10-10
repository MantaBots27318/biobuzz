package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Check d'avant-match : teste un par un chaque moteur, servo et servo continu de la config active,
 * sans rien connaître du robot. À lancer dans les pits après chaque réparation.
 *
 * Manette 1 : gauche/droite du pad = appareil précédent/suivant.
 * Moteur ou servo continu : A maintenu = sens +, B maintenu = sens -.
 * Servo : Y = position 0,5, haut/bas du pad = +/- 0,05. Un servo n'est commandé qu'après un appui,
 * pour ne pas forcer un mécanisme en butée.
 */
@TeleOp(name = "System Check", group = "Test")
public class SystemCheck extends OpMode {
    private static final double MOTOR_TEST_POWER = 0.3;
    private static final double SERVO_STEP = 0.05;

    private final List<HardwareDevice> devices = new ArrayList<>();
    private final List<String> names = new ArrayList<>();
    // Position envoyée à chaque servo, suivie ici car getPosition() ne dit rien tant qu'on n'a rien envoyé
    private final Map<Servo, Double> servoTargets = new HashMap<>();
    private int index = 0;
    private boolean lastLeft, lastRight, lastUp, lastDown;

    @Override
    public void init() {
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
            telemetry.addLine("Aucun moteur ni servo dans la config active");
            telemetry.update();
            return;
        }

        if (gamepad1.dpad_right && !lastRight) select(1);
        if (gamepad1.dpad_left && !lastLeft) select(-1);

        HardwareDevice device = devices.get(index);
        telemetry.addData("Appareil", "%d / %d : %s", index + 1, devices.size(), names.get(index));
        telemetry.addData("Branché sur", device.getConnectionInfo());

        if (device instanceof DcMotorEx) {
            DcMotorEx motor = (DcMotorEx) device;
            motor.setPower(direction() * MOTOR_TEST_POWER);
            telemetry.addData("Type", "moteur");
            telemetry.addData("Encodeur", motor.getCurrentPosition());
            telemetry.addData("Courant", "%.2f A", motor.getCurrent(CurrentUnit.AMPS));
        } else if (device instanceof CRServo) {
            ((CRServo) device).setPower(direction() * MOTOR_TEST_POWER);
            telemetry.addData("Type", "servo continu");
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
            telemetry.addData("Position", target == null ? "pas encore commandé" : String.format("%.2f", target));
        }

        telemetry.addLine();
        telemetry.addLine("Pad gauche/droite : changer d'appareil");
        telemetry.addLine("A / B maintenu : faire tourner (moteur, servo continu)");
        telemetry.addLine("Y, pad haut/bas : bouger (servo)");
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
