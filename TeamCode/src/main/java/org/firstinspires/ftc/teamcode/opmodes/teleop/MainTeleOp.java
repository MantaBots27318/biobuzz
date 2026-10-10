package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.seattlesolvers.solverslib.command.CommandOpMode;
import com.seattlesolvers.solverslib.command.InstantCommand;
import com.seattlesolvers.solverslib.gamepad.GamepadEx;
import com.seattlesolvers.solverslib.gamepad.GamepadKeys;

import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.util.LoopTimer;
import org.firstinspires.ftc.teamcode.util.TelemetryUtil;

/**
 * TeleOp de base, sans Pedro : pilotage par rapport au robot et servo de la barre.
 *
 * Manette 1 : stick gauche = avancer / translater, stick droit (gauche-droite) = tourner,
 * gâchette haute droite maintenue = mode lent, A = barre en position active, B = barre au repos,
 * Y = alterner entre les deux.
 */
// Le préfixe « A. » fait apparaître l'OpMode en tête de liste sur le Driver Hub
@TeleOp(name = "A. TeleOp", group = "Match")
public class MainTeleOp extends CommandOpMode {
    private Robot robot;
    private final LoopTimer loopTimer = new LoopTimer();

    @Override
    public void initialize() {
        telemetry = TelemetryUtil.withPanels(telemetry);
        reset();
        robot = new Robot(hardwareMap);

        GamepadEx driver = new GamepadEx(gamepad1);
        driver.getGamepadButton(GamepadKeys.Button.A)
                .whenPressed(new InstantCommand(robot.bar::activate, robot.bar));
        driver.getGamepadButton(GamepadKeys.Button.B)
                .whenPressed(new InstantCommand(robot.bar::rest, robot.bar));
        driver.getGamepadButton(GamepadKeys.Button.Y)
                .whenPressed(new InstantCommand(robot.bar::toggle, robot.bar));
    }

    @Override
    public void run() {
        // Pousser le stick vers l'avant donne une valeur négative, d'où le signe moins
        robot.drive.drive(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x,
                gamepad1.right_bumper);
        super.run();

        loopTimer.tick();
        telemetry.addData("Boucle", "%.0f Hz", loopTimer.hz());
        telemetry.addData("Batterie", "%.1f V", robot.battery.volts());
        telemetry.addData("Mode", gamepad1.right_bumper ? "lent" : "normal");
        telemetry.addData("Barre", robot.bar.isActive() ? "active" : "repos");
        telemetry.update();
    }
}
