package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.seattlesolvers.solverslib.command.CommandOpMode;
import com.seattlesolvers.solverslib.command.StartEndCommand;
import com.seattlesolvers.solverslib.gamepad.GamepadEx;
import com.seattlesolvers.solverslib.gamepad.GamepadKeys;

import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.LoopTimer;
import org.firstinspires.ftc.teamcode.util.MatchState;

// Le préfixe « A. » fait apparaître l'OpMode en tête de liste sur le Driver Hub
@TeleOp(name = "A. TeleOp", group = "Match")
public class MainTeleOp extends CommandOpMode {
    private Robot robot;
    private Alliance alliance;
    private boolean poseFromAuto;
    private final LoopTimer loopTimer = new LoopTimer();

    @Override
    public void initialize() {
        reset();
        robot = new Robot(hardwareMap);

        // Si un Auto vient de tourner, on reprend son alliance et sa position
        poseFromAuto = MatchState.isFresh();
        alliance = poseFromAuto ? MatchState.alliance() : Alliance.BLUE;

        GamepadEx driver = new GamepadEx(gamepad1);
        GamepadEx operator = new GamepadEx(gamepad2);

        // Pilote : à utiliser quand le robot est tourné dos au pilote
        driver.getGamepadButton(GamepadKeys.Button.BACK).whenPressed(robot.drivetrain::resetHeading);

        // Opérateur : l'intake tourne tant que le bouton est maintenu
        operator.getGamepadButton(GamepadKeys.Button.A)
                .whenHeld(new StartEndCommand(robot.intake::collect, robot.intake::stop, robot.intake));
        operator.getGamepadButton(GamepadKeys.Button.B)
                .whenHeld(new StartEndCommand(robot.intake::eject, robot.intake::stop, robot.intake));
    }

    @Override
    public void initialize_loop() {
        if (gamepad1.x) alliance = Alliance.BLUE;
        if (gamepad1.b) alliance = Alliance.RED;
        telemetry.addData("Alliance", "%s   (X = bleu, B = rouge)", alliance);
        telemetry.addData("Position", poseFromAuto ? "reprise de l'Auto" : "robot à placer dos au pilote");
        telemetry.update();
    }

    @Override
    public void preRun() {
        robot.drivetrain.setAlliance(alliance);
        if (poseFromAuto) {
            robot.drivetrain.setPose(MatchState.pose());
        } else {
            robot.drivetrain.resetHeading();
        }
    }

    @Override
    public void run() {
        // Les axes de la manette sont inversés par rapport au repère Pedro, d'où les signes moins.
        // On donne la consigne avant super.run() pour que follower.update() l'applique dans la même boucle.
        robot.drivetrain.driveFieldCentric(-gamepad1.left_stick_y, -gamepad1.left_stick_x, -gamepad1.right_stick_x);
        super.run();

        loopTimer.tick();
        telemetry.addData("Boucle", "%.0f Hz", loopTimer.hz());
        telemetry.addData("Batterie", "%.1f V", robot.battery.volts());
        telemetry.addData("Pose", robot.drivetrain.pose());
        telemetry.update();
    }
}
