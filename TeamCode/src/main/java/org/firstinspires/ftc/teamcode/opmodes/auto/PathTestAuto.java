package org.firstinspires.ftc.teamcode.opmodes.auto;

import static com.pedropathing.api.Paths.curve;
import static com.pedropathing.api.Paths.line;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.seattlesolvers.solverslib.command.Command;
import com.seattlesolvers.solverslib.command.CommandOpMode;
import com.seattlesolvers.solverslib.command.CommandScheduler;
import com.seattlesolvers.solverslib.command.SequentialCommandGroup;
import com.seattlesolvers.solverslib.pedroCommand.FollowPathCommand;

import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.LoopTimer;
import org.firstinspires.ftc.teamcode.util.TelemetryUtil;

/**
 * Path-following test: runs a line, two curves and a final line in a row to check the Pedro tuning on the field.
 * Blue alliance only, so it can be started from Panels without a gamepad.
 * Needs only the four drive motors and the Pinpoint in the hardware configuration.
 * Exception to the "go through Robot" rule, like OuttakeTest.
 */
@Configurable
@Autonomous(name = "Path Test Auto", group = "Test")
public class PathTestAuto extends CommandOpMode {
    /** Time limit per path: if the robot is blocked, move on to the next one. */
    public static long PATH_TIMEOUT_MS = 8000;

    private Drivetrain drivetrain;
    private static final Alliance ALLIANCE = Alliance.BLUE;

    private final LoopTimer loopTimer = new LoopTimer();

    @Override
    public void initialize() {
        telemetry = TelemetryUtil.withPanels(telemetry);
        reset();
        // Only the drivetrain and the Pinpoint are needed, so this test creates the drivetrain alone
        // instead of Robot, which also needs the mechanisms in the hardware configuration
        CommandScheduler.getInstance().setBulkReading(hardwareMap, LynxModule.BulkCachingMode.MANUAL);
        drivetrain = new Drivetrain(hardwareMap);
        register(drivetrain);
    }

    @Override
    public void preRun() {
        PoseFactory p = ALLIANCE.poses();
        Pose start = p.of(60, 133, 270);
        Pose point1 = p.of(60, 106, 270);
        Pose point2 = p.of(47.3509, 129.3704, 270);
        Pose control2 = p.of(43.8635, 126.6842, 0);
        Pose point3 = p.of(59.729, 105.4405, 270);
        Pose control3 = p.of(43.8977, 126.4795, 0);
        Pose point4 = p.of(10.9727, 118.268, -14.7401);

        drivetrain.setAlliance(ALLIANCE);
        drivetrain.setPose(start);

        // Add the arm / servo actions between the paths if needed
        schedule(new SequentialCommandGroup(
                follow(line(start, point1).linear(start, point1)),
                follow(curve(point1, control2, point2).linear(point1, point2)),
                follow(curve(point2, control3, point3).linear(point2, point3)),
                follow(line(point3, point4).reverseTangent())
        ));
    }

    @Override
    public void run() {
        super.run();

        loopTimer.tick();
        telemetry.addData("Alliance", ALLIANCE);
        telemetry.addData("Loop", "%.0f Hz", loopTimer.hz());
        telemetry.addData("Pose", drivetrain.pose());
        telemetry.update();
    }

    private Command follow(Path path) {
        return new FollowPathCommand(drivetrain.follower, path).withTimeout(PATH_TIMEOUT_MS);
    }
}
