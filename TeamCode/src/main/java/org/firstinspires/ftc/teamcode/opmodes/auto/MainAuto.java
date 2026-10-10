package org.firstinspires.ftc.teamcode.opmodes.auto;

import static com.pedropathing.api.Paths.line;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.seattlesolvers.solverslib.command.Command;
import com.seattlesolvers.solverslib.command.CommandOpMode;
import com.seattlesolvers.solverslib.command.DeferredCommand;
import com.seattlesolvers.solverslib.command.InstantCommand;
import com.seattlesolvers.solverslib.command.SequentialCommandGroup;
import com.seattlesolvers.solverslib.command.WaitCommand;
import com.seattlesolvers.solverslib.pedroCommand.FollowPathCommand;

import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.LoopTimer;
import org.firstinspires.ftc.teamcode.util.MatchState;
import org.firstinspires.ftc.teamcode.util.TelemetryUtil;

/**
 * A single Auto for both alliances: the alliance is chosen during init, and the poses
 * (written for the blue side) are transformed by Alliance.poses().
 */
@Configurable
@Autonomous(name = "A. Auto", group = "Match", preselectTeleOp = "A. TeleOp")
public class MainAuto extends CommandOpMode {
    /** Time limit per path: if the robot is blocked (partner, opponent), move on to the next step. */
    public static long PATH_TIMEOUT_MS = 5000;
    /** Time limit for the scoring part: past it, give up and go park. */
    public static long SCORING_TIMEOUT_MS = 25000;

    private Robot robot;
    private Alliance alliance = Alliance.BLUE;
    private final LoopTimer loopTimer = new LoopTimer();

    @Override
    public void initialize() {
        telemetry = TelemetryUtil.withPanels(telemetry);
        reset();
        robot = new Robot(hardwareMap);
    }

    @Override
    public void initialize_loop() {
        if (gamepad1.x) alliance = Alliance.BLUE;
        if (gamepad1.b) alliance = Alliance.RED;
        telemetry.addData("Alliance", "%s   (X = blue, B = red)", alliance);
        telemetry.update();
    }

    @Override
    public void preRun() {
        PoseFactory p = alliance.poses();
        // TODO BIOBUZZ poses, blue side, in inches and degrees
        Pose start = p.of(9, 111, -90);
        Pose score = p.of(16, 128, -45);
        Pose park = p.of(68, 96, -90);

        robot.drivetrain.setAlliance(alliance);
        robot.drivetrain.setPose(start);

        Command scoring = new SequentialCommandGroup(
                follow(line(start, score).linear(start, score)),
                new InstantCommand(robot.intake::eject, robot.intake),
                new WaitCommand(500),
                new InstantCommand(robot.intake::stop, robot.intake)
        ).withTimeout(SCORING_TIMEOUT_MS);

        // Park from the actual pose, since the timeout can cut the sequence anywhere
        schedule(new SequentialCommandGroup(
                scoring,
                new InstantCommand(robot.intake::stop, robot.intake),
                new DeferredCommand(() -> {
                    Pose here = robot.drivetrain.pose();
                    return new FollowPathCommand(robot.drivetrain.follower, line(here, park).linear(here, park), false);
                }, null)
        ));
    }

    @Override
    public void run() {
        super.run();
        // Every loop, so the TeleOp gets the right pose even if the Auto is stopped early
        MatchState.save(alliance, robot.drivetrain.pose());

        loopTimer.tick();
        telemetry.addData("Alliance", alliance);
        telemetry.addData("Loop", "%.0f Hz", loopTimer.hz());
        telemetry.addData("Pose", robot.drivetrain.pose());
        telemetry.update();
    }

    private Command follow(Path path) {
        return new FollowPathCommand(robot.drivetrain.follower, path).withTimeout(PATH_TIMEOUT_MS);
    }
}
