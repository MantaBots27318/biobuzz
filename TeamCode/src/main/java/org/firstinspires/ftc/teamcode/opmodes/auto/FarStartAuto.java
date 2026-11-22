package org.firstinspires.ftc.teamcode.opmodes.auto;

import static com.pedropathing.api.Paths.line;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.seattlesolvers.solverslib.command.Command;
import com.seattlesolvers.solverslib.command.CommandOpMode;
import com.seattlesolvers.solverslib.command.CommandScheduler;
import com.seattlesolvers.solverslib.command.ConditionalCommand;
import com.seattlesolvers.solverslib.command.DeferredCommand;
import com.seattlesolvers.solverslib.command.InstantCommand;
import com.seattlesolvers.solverslib.command.ParallelRaceGroup;
import com.seattlesolvers.solverslib.command.SequentialCommandGroup;
import com.seattlesolvers.solverslib.command.WaitCommand;
import com.seattlesolvers.solverslib.command.WaitUntilCommand;
import com.seattlesolvers.solverslib.pedroCommand.FollowPathCommand;

import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.subsystems.HiveAim;
import org.firstinspires.ftc.teamcode.subsystems.Hives;
import org.firstinspires.ftc.teamcode.subsystems.TagLocalizer;
import org.firstinspires.ftc.teamcode.subsystems.Vision;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.AllianceChoice;
import org.firstinspires.ftc.teamcode.vision.TagMap;
import org.firstinspires.ftc.teamcode.util.CsvLog;
import org.firstinspires.ftc.teamcode.util.LoopTimer;
import org.firstinspires.ftc.teamcode.util.MatchState;
import org.firstinspires.ftc.teamcode.util.TelemetryUtil;

/**
 * "Far start": the partner shoots first and tips our HIVE; we wait for the Limelight to see our cell up, shoot, and
 * park. (The FLOWER pickup and a second shot were taken out on 2026-11-14 to test the first part; the routes are
 * in docs/AUTO-PLANNING-NOTES.md.) Shooting is a placeholder (just a wait) until the mechanism exists.
 *
 * The poses are written for the blue side (the same Auto serves both alliances). The blue start is on the
 * audience side, which is the red "far" start rotated by 180 degrees: red (60, 133, 270) = blue (84, 11, 90).
 * The shooter is at the front: the front faces the HIVE to shoot.
 * Not tested on the final robot: the coordinates come from the co-coder's drawing and are not timed.
 */
@Configurable
@Autonomous(name = "A. Auto: far start", group = "Match", preselectTeleOp = "A. TeleOp")
public class FarStartAuto extends CommandOpMode {
    /** Time limit per path: if the robot is blocked, move on to the next step. */
    public static long PATH_TIMEOUT_MS = 5000;
    /**
     * Match clock, seconds from START (2026-11-22, user): the robot waits for our HIVE to tip as long as it can, and
     * the scoring part ends at PARK_START_BY_S at the latest; a park started that late drives with no speed limit
     * (hitting the wall matters less than missing the zone). Everything stops at AUTO_END_S, before the 30 s end.
     * (The old 9 s tip wait and 20 s scoring limit were placeholders and are gone.)
     */
    public static double PARK_START_BY_S = 26;
    public static double AUTO_END_S = 29.5;
    /** How far the robot leaves the wall before it waits and shoots (from the start). Tags must stay in view. */
    public static double SHOOT_DISTANCE_IN = 8;
    public static boolean USE_TAG_POSITION_FIX = false;
    // Both true in a match. For tests only: USE_CAMERA false = no Limelight and no aim, a plain wait; USE_AIM false =
    // the Limelight runs (HIVE watching, log) but the aim step is a plain wait
    public static boolean USE_CAMERA = true;
    public static boolean USE_AIM = true;
    public static long CAMERA_OFF_WAIT_MS = 3000;
    // Off (user, 2026-11-22): the camera stays on during the park. Stopping it was a workaround while the park ended
    // 15 to 20 deg turned; the real cause was the tag fix writing an old heading to the Pinpoint, now fixed (with the
    // camera on the park is parallel and the loop 7 to 19 ms). Kept as a switch
    public static boolean STOP_CAMERA_BEFORE_PARK = false;
    // About 60% of the tuned top speed (68.7 in/s forward, 47.9 strafe). Above that the odometry pods lift when the
    // robot changes direction hard (user, 2026-11-21) and the Pinpoint loses distance. 0 = no limit.
    public static double MAX_SPEED_IN_PER_S = 40;
    /**
     * Park so that the robot's front edge ends PARK_EDGE_GAP_IN from the wall: the LOADING ZONE is 14.75 in. deep and
     * only part of the robot has to be inside it. Centre distance from the wall = edge gap + ROBOT_HALF_LENGTH_IN.
     * ROBOT_HALF_LENGTH_IN is from the Pinpoint's tracked point to the front edge.
     * 2026-11-21: the tracked point is about 3.4 in. behind the robot's middle, so the front edge is 8.5 + 3.4 = 12 in.
     * ahead of it. Checked 2026-11-21 with the camera off: the Pinpoint's end position matched the real one to 1 to 2 in.
     * Re-measure it after any change to the Pinpoint offsets or the robot.
     */
    public static double PARK_EDGE_GAP_IN = 3.5;
    /**
     * The real LOADING ZONE (measured with a tape 2026-11-21, blue coordinates): 14.75 in. deep, 24 in. wide, from
     * y = 22.5 to 46.5. The robot (16.5 in. wide when facing the wall) parks at the far side of it: its side
     * PARK_SIDE_GAP_IN inside the tape nearest the far wall, so its middle is at 22.5 + gap + 8.25.
     */
    public static double ZONE_FAR_EDGE_Y_IN = 22.5;
    public static double PARK_SIDE_GAP_IN = 1.5;
    public static double ROBOT_HALF_WIDTH_IN = 8.25;
    // Only drive again if the last park drive stopped this far short (Pedro once ended a path a third of the way).
    // Small values make the robot push again into the wall when the Pinpoint thinks it is not there yet
    public static double PARK_TOLERANCE_IN = 6;
    // Game speed (user, 2026-11-22: at 30 in/s the park was "very cramped up"). The 2026-11-21 bash at 40 in/s was
    // with the heading going wrong; the park starts from a stop, so the pods-lift limit (hard direction changes) is
    // not hit. If it hits the wall hard, lower it
    public static double PARK_SPEED_IN_PER_S = 50;
    // A park takes about 3 s: a short limit stops the robot pushing on the wall if it arrives early
    public static long PARK_TIMEOUT_MS = 4000;
    public static double ROBOT_HALF_LENGTH_IN = 12;
    /** Placeholder for the real shooter. */
    public static long SHOOT_TIME_MS = 1500;
    /** Rows written to the log per second at most (0.04 s apart = 25 per second). */
    public static long LOG_PERIOD_MS = 40;

    // Blue-side poses (inches, degrees): the co-coder's red route rotated by 180 degrees about (72, 72).
    // The shoot spot is SHOOT_DISTANCE_IN straight ahead of the start (it replaces the co-coder's (60, 106)).
    // Measured 2026-11-21: robot 16.5 x 17 in., back against the far wall, left edge 2 in. from the centre line.
    // The Pinpoint's tracked point is about 3.4 in. BEHIND the robot's middle (Path Check, Y), so it is 5.1 in. from the back edge
    private static final double[] START = {82.25, 5.1, 90};
    // Park spot: blue x is 144 minus the centre distance from the wall, computed in preRun()
    private static final double[] PARK = {0, 0, 0};

    // Only what this Auto needs (shoot and intake are placeholders), so the configuration needs no intake
    private Drivetrain drivetrain;
    private Vision vision;
    private Hives hives;
    private HiveAim aim;
    private TagLocalizer localizer;
    private Hives.Cluster target = Hives.Cluster.BLUE_AUDIENCE;
    private final AllianceChoice choice = new AllianceChoice();
    private Alliance alliance = AllianceChoice.DEFAULT;
    private final LoopTimer loopTimer = new LoopTimer();
    private final CsvLog log = new CsvLog();
    private final ElapsedTime runTime = new ElapsedTime();
    private long lastLogMs = Long.MIN_VALUE / 2;
    private String step = "waiting for START";

    @Override
    public void initialize() {
        telemetry = TelemetryUtil.withPanels(telemetry);
        reset();
        // Bulk reads, as in Robot: one grouped read per hub per loop, the scheduler clears the cache each run()
        CommandScheduler.getInstance().setBulkReading(hardwareMap, LynxModule.BulkCachingMode.MANUAL);
        drivetrain = new Drivetrain(hardwareMap);
        if (MAX_SPEED_IN_PER_S > 0) speedLimit().set(MAX_SPEED_IN_PER_S);
        vision = new Vision(hardwareMap);
        hives = new Hives(vision);
        aim = new HiveAim(vision, hives);
        localizer = new TagLocalizer(drivetrain, vision, hives);
        // Vision first, then Hives, then the others: each reads the one before it
        // The position correction stays off until the camera is mounted for real: with the taped Limelight it moved
        // the Pinpoint 5 in. wrong in y (2026-11-21) and the park ended past the zone
        // Every subsystem registers itself when it is created (SolversLib's SubsystemBase), so the ones not wanted are
        // taken out. Until 2026-11-22 the tag position fix ran in this Auto even with USE_TAG_POSITION_FIX false
        CommandScheduler scheduler = CommandScheduler.getInstance();
        if (!USE_CAMERA) scheduler.unregisterSubsystem(vision, hives, aim, localizer);
        else if (!USE_TAG_POSITION_FIX) scheduler.unregisterSubsystem(localizer);
    }

    @Override
    public void initialize_loop() {
        choice.update(gamepad1);
        alliance = choice.get();
        telemetry.addLine(choice.line());
        telemetry.update();
    }

    @Override
    public void preRun() {
        PoseFactory p = alliance.poses();
        Pose start = pose(p, START);
        // Straight ahead of the start, facing the HIVE (blue coordinates: +y is away from the audience)
        Pose spot = pose(p, new double[]{START[0], START[1] + SHOOT_DISTANCE_IN, START[2]});
        Pose park = p.of(144 - (PARK_EDGE_GAP_IN + ROBOT_HALF_LENGTH_IN),
                ZONE_FAR_EDGE_Y_IN + PARK_SIDE_GAP_IN + ROBOT_HALF_WIDTH_IN, PARK[2]);
        // Where our HIVE is (its pivot), for facing it from the Pinpoint while the cell is not up
        Pose hive = p.of(TagMap.PIVOT_X_IN, TagMap.PIVOT_Y_IN, 0);

        drivetrain.setAlliance(alliance);
        aim.setAlliance(alliance);
        drivetrain.setPose(start);
        localizer.setPoseKnown(true);
        if (USE_CAMERA) vision.start();
        log.open("farstart", "t_ms,alliance,step,x,y,heading_deg,target,target_state,blue_far,blue_aud,red_far,red_aud,"
                + "aim_target,aim_has_target,aim_error_deg,turn_power,tags_seen,tag_age_ms,hive_info,hive_rule,far_info,aud_info,"
                + "loc_x,loc_y,loc_error_in,loc_gain,loc_status,heading_disagreement_deg,speed_in_s,follower_busy,loop_ms");
        runTime.reset();

        // The cell nearest to our start side is the one the partner's shot brings up: red starts far, blue audience
        target = alliance == Alliance.RED ? Hives.Cluster.RED_FAR : Hives.Cluster.BLUE_AUDIENCE;
        AimAndWaitForCell firstWait = new AimAndWaitForCell(drivetrain, hives, aim, target,
                hive.x(), hive.y(), Long.MAX_VALUE); // no own limit: the match clock ends the wait

        Command scoring = new SequentialCommandGroup(
                label("to the shoot spot"),
                follow(line(start, spot).linear(start, spot)),
                label("waiting for the HIVE to tip"),
                USE_CAMERA && USE_AIM ? new SequentialCommandGroup(
                        firstWait,
                        // Never shoot without having seen the cell up: otherwise go straight to PARK
                        new ConditionalCommand(shoot(), new InstantCommand(), firstWait::succeeded))
                        : new SequentialCommandGroup(new WaitCommand(CAMERA_OFF_WAIT_MS), shoot())
        );

        // Park from the actual pose, since a timeout can cut the sequence anywhere. A cut wait step counts as
        // interrupted, so it never shoots
        schedule(new SequentialCommandGroup(
                new ParallelRaceGroup(scoring, untilClock(PARK_START_BY_S)),
                new InstantCommand(() -> {
                    boolean late = runTime.seconds() >= PARK_START_BY_S;
                    step = late ? "parking (late)" : "parking";
                    if (STOP_CAMERA_BEFORE_PARK) vision.stop();
                    if (late) speedLimit().set(ForesightConfig.Constraint.NONE);
                    else speedLimit().set(PARK_SPEED_IN_PER_S);
                }),
                // Pedro once ended the park path a third of the way (2026-11-21, cause unknown): try again from
                // wherever the robot is until it is close enough
                new ParallelRaceGroup(
                        new SequentialCommandGroup(parkLeg(park), parkLeg(park), parkLeg(park)),
                        untilClock(AUTO_END_S)),
                new InstantCommand(() -> {
                    if (runTime.seconds() >= AUTO_END_S) {
                        step = "time up";
                        drivetrain.follower.hold(drivetrain.pose());
                    } else {
                        step = "done";
                    }
                })
        ));
    }

    @Override
    public void run() {
        super.run();
        // Every loop, so the TeleOp gets the right pose even if the Auto is stopped early
        MatchState.save(alliance, drivetrain.pose());

        logRow();

        loopTimer.tick();
        telemetry.addData("Alliance", alliance);
        telemetry.addData("Step", step);
        telemetry.addData("Pose", drivetrain.pose());
        telemetry.addData("Tags", localizer.getStatus());
        telemetry.addData("Aim", "%s, error %.1f deg%s", aim.getTargetName(), aim.getErrorDeg(),
                aim.isAimed() ? " (aimed)" : "");
        telemetry.addData("Target cell", "%s: %s", target, hives.getState(target));
        telemetry.addData("All cells", "BLUE_FAR %s, BLUE_AUD %s, RED_FAR %s, RED_AUD %s",
                hives.getState(Hives.Cluster.BLUE_FAR), hives.getState(Hives.Cluster.BLUE_AUDIENCE),
                hives.getState(Hives.Cluster.RED_FAR), hives.getState(Hives.Cluster.RED_AUDIENCE));
        telemetry.addData("Log", log.getStatus());
        telemetry.addData("Loop", "%.0f Hz", loopTimer.hz());
        telemetry.update();
    }

    @Override
    public void end() {
        log.close();
        vision.stop();
        // The limit lives in a shared setting: give it back so the next OpMode is not slowed
        speedLimit().set(ForesightConfig.Constraint.NONE);
    }

    /** One drive to the park pose from the actual pose, or nothing if the robot is already there. */
    private Command parkLeg(Pose park) {
        return new DeferredCommand(() -> {
            Pose here = drivetrain.pose();
            if (Math.hypot(park.x() - here.x(), park.y() - here.y()) < PARK_TOLERANCE_IN) return new InstantCommand();
            return new FollowPathCommand(drivetrain.follower, line(here, park).linear(here, park), false)
                    .withTimeout(PARK_TIMEOUT_MS);
        }, null);
    }

    private com.pedropathing.config.ConfigVar<Double> speedLimit() {
        return ((Foresight) drivetrain.follower.algorithm()).config.maxVelocityConstraint;
    }

    private void logRow() {
        long now = (long) runTime.milliseconds();
        if (now - lastLogMs < LOG_PERIOD_MS) return;
        lastLogMs = now;
        Pose pose = drivetrain.pose();
        StringBuilder ids = new StringBuilder();
        for (Vision.Tag tag : vision.getTags()) ids.append(tag.id).append(' ');
        log.row(now, alliance, step, String.format("%.1f", pose.x()), String.format("%.1f", pose.y()),
                String.format("%.1f", Math.toDegrees(pose.heading())), target, hives.getState(target),
                hives.getState(Hives.Cluster.BLUE_FAR), hives.getState(Hives.Cluster.BLUE_AUDIENCE),
                hives.getState(Hives.Cluster.RED_FAR), hives.getState(Hives.Cluster.RED_AUDIENCE),
                aim.getTargetName(), aim.hasTarget(), String.format("%.1f", aim.getErrorDeg()),
                String.format("%.2f", aim.turnPower()), ids.toString().trim(), vision.getAgeMs(),
                hives.getInfo(target), hives.getRule(target.hive),
                hives.getInfo(Hives.Cluster.valueOf(target.hive + "_FAR")),
                hives.getInfo(Hives.Cluster.valueOf(target.hive + "_AUDIENCE")),
                String.format("%.1f", localizer.getMeasuredX()),
                String.format("%.1f", localizer.getMeasuredY()), String.format("%.1f", localizer.getLastErrorIn()),
                String.format("%.2f", localizer.getLastGain()), localizer.getStatus(),
                String.format("%.1f", localizer.getHeadingDisagreementDeg()),
                String.format("%.1f", Math.hypot(drivetrain.follower.velocity().vx, drivetrain.follower.velocity().vy)),
                drivetrain.follower.isBusy(), String.format("%.0f", loopTimer.ms()));
    }

    private static Pose pose(PoseFactory p, double[] xyDeg) {
        return p.of(xyDeg[0], xyDeg[1], xyDeg[2]);
    }

    private Command follow(Path path) {
        return new FollowPathCommand(drivetrain.follower, path).withTimeout(PATH_TIMEOUT_MS);
    }

    /** Ends when the match clock (seconds from START) reaches the given time. */
    private Command untilClock(double seconds) {
        return new WaitUntilCommand(() -> runTime.seconds() >= seconds);
    }

    /** Shows the current step in the telemetry. */
    private Command label(String text) {
        return new InstantCommand(() -> step = text);
    }

    /** Placeholder: the real shooter will replace the wait. */
    private Command shoot() {
        return new SequentialCommandGroup(label("SHOOT (placeholder)"), new WaitCommand(SHOOT_TIME_MS));
    }
}
