package org.firstinspires.ftc.teamcode.opmodes.test;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.seattlesolvers.solverslib.command.CommandOpMode;

import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.util.TelemetryUtil;

/**
 * Checks the Pinpoint's distances against a tape measure. Press Cross to zero, then push (or drive) the robot a known
 * distance, forward or sideways, and read how far the Pinpoint says it went, in the robot's own directions, next to
 * the distance you measured. Needs only the drive motors and the Pinpoint in the configuration.
 */
@Configurable
@TeleOp(name = "Odometry Check", group = "Test")
public class OdometryCheck extends CommandOpMode {
    /** The distance you move the robot by hand, measured with a tape measure. */
    public static double EXPECTED_IN = 48;
    // Below this a percentage means nothing
    public static double MIN_DISTANCE_IN = 10;

    private Drivetrain drivetrain;
    private Pose zero = new Pose(0, 0, 0);
    private boolean lastCross;

    @Override
    public void initialize() {
        reset();
        telemetry = TelemetryUtil.withPanels(telemetry);
        drivetrain = new Drivetrain(hardwareMap);
        register(drivetrain);
        telemetry.addLine("Put the robot at the start of your tape measure, press START, then Cross to zero");
        telemetry.update();
    }

    @Override
    public void preRun() {
        drivetrain.setPose(new Pose(0, 0, 0));
    }

    @Override
    public void run() {
        drivetrain.driveFieldCentric(-gamepad1.left_stick_y, -gamepad1.left_stick_x, -gamepad1.right_stick_x);
        super.run();

        Pose now = drivetrain.pose();
        if (gamepad1.cross && !lastCross) zero = now;
        lastCross = gamepad1.cross;

        // The move in the robot's own directions at the moment of zeroing: forward, and to the left
        double dx = now.x() - zero.x();
        double dy = now.y() - zero.y();
        double forward = dx * Math.cos(zero.heading()) + dy * Math.sin(zero.heading());
        double left = -dx * Math.sin(zero.heading()) + dy * Math.cos(zero.heading());
        double turned = Math.toDegrees(now.heading() - zero.heading());

        telemetry.addData("Forward", "%.1f in%s", forward, percent(forward));
        telemetry.addData("Left", "%.1f in%s", left, percent(left));
        telemetry.addData("Turned", "%.1f deg (keep the robot pointing the same way)", turned);
        telemetry.addData("Expected", "%.0f in (change EXPECTED_IN if you measured another distance)", EXPECTED_IN);
        telemetry.addLine("Cross = zero. Push straight forward, read, Cross again, push straight left, read.");
        telemetry.addLine("Spin test: Cross, turn in place one full turn (right stick), Forward and Left should be about 0, Turned about 360");
        telemetry.update();
    }

    private static String percent(double moved) {
        if (Math.abs(moved) < MIN_DISTANCE_IN) return "";
        return String.format("  = %.0f%% of the expected distance", 100 * Math.abs(moved) / EXPECTED_IN);
    }
}
