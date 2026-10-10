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
        telemetry.addData("Pose", poseFromAuto ? "taken from the Auto" : "place the robot facing away from the driver");
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
        // The gamepad axes are inverted relative to Pedro's frame, hence the minus signs.
        // Set the input before super.run() so follower.update() applies it in the same loop.
        robot.drivetrain.driveFieldCentric(-gamepad1.left_stick_y, -gamepad1.left_stick_x, -gamepad1.right_stick_x);
        super.run();

        loopTimer.tick();
        telemetry.addData("Loop", "%.0f Hz", loopTimer.hz());
        telemetry.addData("Battery", "%.1f V", robot.battery.volts());
        telemetry.addData("Pose", robot.drivetrain.pose());
        telemetry.update();
    }
}
