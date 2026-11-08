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
import org.firstinspires.ftc.teamcode.util.TelemetryUtil;

// The "A." prefix puts the OpMode at the top of the Driver Hub list
@TeleOp(name = "A. TeleOp", group = "Match")
public class MainTeleOp extends CommandOpMode {
    private Robot robot;
    private Alliance alliance;
    private boolean poseFromAuto;
    private final LoopTimer loopTimer = new LoopTimer();

    @Override
    public void initialize() {
        telemetry = TelemetryUtil.withPanels(telemetry);
        reset();
        robot = new Robot(hardwareMap);

        // If an Auto just ran, reuse its alliance and pose
        poseFromAuto = MatchState.isFresh();
        alliance = poseFromAuto ? MatchState.alliance() : Alliance.BLUE;

        GamepadEx driver = new GamepadEx(gamepad1);
        GamepadEx operator = new GamepadEx(gamepad2);

        // Driver: use when the robot faces away from the driver
        driver.getGamepadButton(GamepadKeys.Button.BACK).whenPressed(robot.drivetrain::resetHeading);

        // Operator: the intake runs while the button is held
        operator.getGamepadButton(GamepadKeys.Button.A)
                .whenHeld(new StartEndCommand(robot.intake::collect, robot.intake::stop, robot.intake));
        operator.getGamepadButton(GamepadKeys.Button.B)
                .whenHeld(new StartEndCommand(robot.intake::eject, robot.intake::stop, robot.intake));
    }

    @Override
    public void initialize_loop() {
        if (gamepad1.x) alliance = Alliance.BLUE;
        if (gamepad1.b) alliance = Alliance.RED;
        telemetry.addData("Alliance", "%s   (X = blue, B = red)", alliance);
        telemetry.addData("Pose", poseFromAuto ? "taken from the Auto"
                : robot.localizer.hasTeleOpStart() ? "set to the TeleOp start spot (see the settings)"
                : "place the robot facing away from the driver");
        telemetry.update();
    }

    @Override
    public void preRun() {
        robot.drivetrain.setAlliance(alliance);
        robot.aim.setAlliance(alliance);
        robot.vision.start();
        if (poseFromAuto) {
            robot.drivetrain.setPose(MatchState.pose());
            robot.localizer.setPoseKnown(true);
        } else if (robot.localizer.hasTeleOpStart()) {
            robot.drivetrain.setPose(robot.localizer.teleOpStartPose(alliance));
            robot.localizer.setPoseKnown(true);
        } else {
            // Position unknown: the first good tag reading sets it
            robot.drivetrain.resetHeading();
            robot.localizer.setPoseKnown(false);
        }
    }

    @Override
    public void run() {
        // The gamepad axes are inverted relative to Pedro's frame, hence the minus signs.
        // Set the input before super.run() so follower.update() applies it in the same loop.
        // Driver: while RIGHT_BUMPER is held and a tag of our HIVE is in view, the robot turns by itself to face it
        boolean aiming = gamepad1.right_bumper && robot.aim.hasTarget();
        double turn = aiming ? robot.aim.turnPower() : -gamepad1.right_stick_x;
        robot.drivetrain.driveFieldCentric(-gamepad1.left_stick_y, -gamepad1.left_stick_x, turn);
        super.run();

        loopTimer.tick();
        telemetry.addData("Loop", "%.0f Hz", loopTimer.hz());
        telemetry.addData("Battery", "%.1f V", robot.battery.volts());
        telemetry.addData("Pose", robot.drivetrain.pose());
        telemetry.addData("Tags", robot.localizer.getStatus());
        telemetry.addData("Aim", "%s, error %.1f deg%s", robot.aim.getTargetName(), robot.aim.getErrorDeg(),
                robot.aim.isAimed() ? " (aimed)" : "");
        telemetry.update();
    }

    @Override
    public void end() {
        robot.vision.stop();
    }
}
