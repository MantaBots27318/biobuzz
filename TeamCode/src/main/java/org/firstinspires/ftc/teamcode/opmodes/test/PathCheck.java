package org.firstinspires.ftc.teamcode.opmodes.test;

import static com.pedropathing.api.Paths.line;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.config.ConfigVar;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.seattlesolvers.solverslib.command.CommandOpMode;
import com.seattlesolvers.solverslib.command.DeferredCommand;
import com.seattlesolvers.solverslib.command.InstantCommand;
import com.seattlesolvers.solverslib.command.SequentialCommandGroup;
import com.seattlesolvers.solverslib.command.WaitCommand;
import com.seattlesolvers.solverslib.gamepad.GamepadEx;
import com.seattlesolvers.solverslib.gamepad.GamepadKeys;
import com.seattlesolvers.solverslib.pedroCommand.FollowPathCommand;

import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.util.TelemetryUtil;

/**
 * Checks Pedro driving a path against a tape measure. Press X to zero, then A: the robot drives a straight line
 * straight ahead (or B: to its left, or Y: sideways while turning like the far-start park leg) and holds the end. Measure how far it really went and compare with what
 * the Pinpoint says. Needs only the drive motors and the Pinpoint. Same speed limit as the far-start Auto.
 */
@Configurable
@TeleOp(name = "Path Check", group = "Test")
public class PathCheck extends CommandOpMode {
    public static double DISTANCE_IN = 48;
    // Same as FarStartAuto.MAX_SPEED_IN_PER_S; 0 = no limit
    public static double MAX_SPEED_IN_PER_S = 40;
    public static double PATH_TIMEOUT_MS = 8000;
    // The far-start Auto starts at 270 and its park turn ends near 180, where the heading wraps; the other tests start at 0
    public static double START_HEADING_DEG = 270;
    // D-pad up replays the red far-start Auto's moves without camera or aim (from its log, 2026-11-21). Start the
    // robot where the Auto starts, facing the audience
    public static double AUTO_LEG_IN = 8;
    public static double AUTO_WAIT_MS = 3000;
    public static double AUTO_PARK_DX_IN = -39.2;
    public static double AUTO_PARK_DY_IN = -30.1;
    public static double AUTO_PARK_HEADING_DEG = 180;
    public static double AUTO_PARK_SPEED_IN_PER_S = 30;
    // Y: the move of the far-start park leg (sideways while turning)
    public static double TURNING_MOVE_IN = 44;
    public static double TURNING_MOVE_TURN_DEG = -88;

    private Drivetrain drivetrain;
    private Pose zero = new Pose(0, 0, 0);

    @Override
    public void initialize() {
        reset();
        telemetry = TelemetryUtil.withPanels(telemetry);
        drivetrain = new Drivetrain(hardwareMap);
        register(drivetrain);
        if (MAX_SPEED_IN_PER_S > 0) speedLimit().set(MAX_SPEED_IN_PER_S);

        GamepadEx driver = new GamepadEx(gamepad1);
        driver.getGamepadButton(GamepadKeys.Button.X).whenPressed(() -> zero = drivetrain.pose());
        driver.getGamepadButton(GamepadKeys.Button.A).whenPressed(drive(1, 0, DISTANCE_IN, 0));
        driver.getGamepadButton(GamepadKeys.Button.B).whenPressed(drive(0, 1, DISTANCE_IN, 0));
        // To the robot's right while turning clockwise, as in the park leg of the far-start Auto
        driver.getGamepadButton(GamepadKeys.Button.Y).whenPressed(drive(0, -1, TURNING_MOVE_IN, TURNING_MOVE_TURN_DEG));
        driver.getGamepadButton(GamepadKeys.Button.DPAD_UP).whenPressed(new SequentialCommandGroup(
                move(0, -AUTO_LEG_IN, Double.NaN, true),
                new WaitCommand((long) AUTO_WAIT_MS),
                new InstantCommand(() -> speedLimit().set(AUTO_PARK_SPEED_IN_PER_S)),
                move(AUTO_PARK_DX_IN, AUTO_PARK_DY_IN, AUTO_PARK_HEADING_DEG, false),
                new InstantCommand(() -> speedLimit().set(MAX_SPEED_IN_PER_S > 0 ? MAX_SPEED_IN_PER_S
                        : ForesightConfig.Constraint.NONE))));

        telemetry.addLine("Put the robot at the start of your tape measure, press START, then X to zero");
        telemetry.update();
    }

    @Override
    public void preRun() {
        drivetrain.setPose(new Pose(0, 0, Math.toRadians(START_HEADING_DEG)));
        zero = drivetrain.pose();
    }

    /** A straight line from where the robot is now, ahead (1, 0), to the left (0, 1) or the right (0, -1), turning on the way. */
    private DeferredCommand drive(double ahead, double left, double distanceIn, double turnDeg) {
        return new DeferredCommand(() -> {
            Pose here = drivetrain.pose();
            double h = here.heading();
            double dx = distanceIn * (ahead * Math.cos(h) - left * Math.sin(h));
            double dy = distanceIn * (ahead * Math.sin(h) + left * Math.cos(h));
            Pose end = new Pose(here.x() + dx, here.y() + dy, h + Math.toRadians(turnDeg));
            return new FollowPathCommand(drivetrain.follower, line(here, end).linear(here, end))
                    .withTimeout((long) PATH_TIMEOUT_MS);
        }, null);
    }

    /** A line by (dx, dy) in field coordinates, ending at headingDeg (NaN: keep the heading). */
    private DeferredCommand move(double dx, double dy, double headingDeg, boolean holdEnd) {
        return new DeferredCommand(() -> {
            Pose here = drivetrain.pose();
            double h = Double.isNaN(headingDeg) ? here.heading() : Math.toRadians(headingDeg);
            Pose end = new Pose(here.x() + dx, here.y() + dy, h);
            return new FollowPathCommand(drivetrain.follower, line(here, end).linear(here, end), holdEnd)
                    .withTimeout((long) PATH_TIMEOUT_MS);
        }, null);
    }

    @Override
    public void run() {
        super.run();

        Pose now = drivetrain.pose();
        double dx = now.x() - zero.x();
        double dy = now.y() - zero.y();
        double forward = dx * Math.cos(zero.heading()) + dy * Math.sin(zero.heading());
        double left = -dx * Math.sin(zero.heading()) + dy * Math.cos(zero.heading());
        double speed = Math.hypot(drivetrain.follower.velocity().vx, drivetrain.follower.velocity().vy);

        telemetry.addData("Pinpoint says", "forward %.1f in, left %.1f in, turned %.1f deg",
                forward, left, Math.toDegrees(angleDiff(now.heading(), zero.heading())));
        telemetry.addData("Heading", "%.1f deg (started at %.0f)", Math.toDegrees(now.heading()), START_HEADING_DEG);
        telemetry.addData("Speed", "%.1f in/s (limit %.0f)", speed, MAX_SPEED_IN_PER_S);
        telemetry.addData("Following a path", drivetrain.follower.isBusy());
        telemetry.addLine("X = zero here. A = drive " + (int) DISTANCE_IN + " in. ahead, B = to the left.");
        telemetry.addLine("Y = " + (int) TURNING_MOVE_IN + " in. to the right while turning " + (int) TURNING_MOVE_TURN_DEG + " deg (the park leg).");
        telemetry.addLine("D-pad up = the far-start Auto's moves (start where the Auto starts).");
        telemetry.addLine("Measure with the tape how far it really went and compare.");
        telemetry.update();
    }

    /** a - b as an angle between -pi and pi. */
    private static double angleDiff(double a, double b) {
        double d = a - b;
        while (d > Math.PI) d -= 2 * Math.PI;
        while (d < -Math.PI) d += 2 * Math.PI;
        return d;
    }

    private ConfigVar<Double> speedLimit() {
        return ((Foresight) drivetrain.follower.algorithm()).config.maxVelocityConstraint;
    }

    @Override
    public void end() {
        // The limit lives in a shared setting: give it back so the next OpMode is not slowed
        speedLimit().set(ForesightConfig.Constraint.NONE);
    }
}
