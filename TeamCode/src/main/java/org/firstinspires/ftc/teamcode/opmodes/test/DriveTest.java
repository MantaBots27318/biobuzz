package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.seattlesolvers.solverslib.command.CommandOpMode;
import com.seattlesolvers.solverslib.command.InstantCommand;
import com.seattlesolvers.solverslib.gamepad.GamepadEx;
import com.seattlesolvers.solverslib.gamepad.GamepadKeys;

import org.firstinspires.ftc.teamcode.subsystems.Bar;
import org.firstinspires.ftc.teamcode.subsystems.RobotCentricDrive;
import org.firstinspires.ftc.teamcode.util.LoopTimer;
import org.firstinspires.ftc.teamcode.util.TelemetryUtil;

/**
 * Pilotage simple sans Pedro, en attendant le Pinpoint. Exception à la règle « on passe par Robot » :
 * Robot crée le Follower Pedro, qui ne démarre pas sans Pinpoint réglé.
 *
 * Manette 1 : stick gauche = avancer / translater, stick droit (gauche-droite) = tourner,
 * gâchette haute droite maintenue = mode lent, Y = barre active, A = barre au repos.
 */
@TeleOp(name = "Drive Test (sans Pedro)", group = "Test")
public class DriveTest extends CommandOpMode {
    private RobotCentricDrive drive;
    private Bar bar;
    private final LoopTimer loopTimer = new LoopTimer();

    @Override
    public void initialize() {
        telemetry = TelemetryUtil.withPanels(telemetry);
        reset();
        drive = new RobotCentricDrive(hardwareMap);
        bar = new Bar(hardwareMap);
        register(drive, bar);

        GamepadEx driver = new GamepadEx(gamepad1);
        driver.getGamepadButton(GamepadKeys.Button.Y).whenPressed(new InstantCommand(bar::activate, bar));
        driver.getGamepadButton(GamepadKeys.Button.A).whenPressed(new InstantCommand(bar::rest, bar));
    }

    @Override
    public void run() {
        // Pousser le stick vers l'avant donne une valeur négative, d'où le signe moins
        drive.drive(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x, gamepad1.right_bumper);
        super.run();

        loopTimer.tick();
        telemetry.addData("Boucle", "%.0f Hz", loopTimer.hz());
        telemetry.addData("Mode", gamepad1.right_bumper ? "lent" : "normal");
        telemetry.addData("Barre", bar.isActive() ? "active" : "repos");
        telemetry.update();
    }
}
