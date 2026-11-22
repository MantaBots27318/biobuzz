package org.firstinspires.ftc.teamcode.opmodes.test;

import static com.pedropathing.api.Paths.line;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.config.ConfigVar;
import com.pedropathing.math.Pose;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.seattlesolvers.solverslib.command.Command;
import com.seattlesolvers.solverslib.command.CommandOpMode;
import com.seattlesolvers.solverslib.command.CommandScheduler;
import com.seattlesolvers.solverslib.pedroCommand.FollowPathCommand;

import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.subsystems.HiveAim;
import org.firstinspires.ftc.teamcode.subsystems.Hives;
import org.firstinspires.ftc.teamcode.subsystems.Vision;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.CsvLog;
import org.firstinspires.ftc.teamcode.util.TelemetryUtil;

import java.util.List;

/**
 * Measures how long each part of the loop takes while Pedro drives, to find why the loop is 3 to 4 times slower
 * with the Limelight seeing tags (piece 7). A: the robot drives PATH_IN ahead and back again and again (put it facing
 * the HIVE tags with room in front); B: stop. Switches on the gamepad (shown on screen): camera, ask rate, our tag
 * processing, log, bulk reads. Needs the drive motors, the Pinpoint and the Limelight only. Writes a log per run.
 */
@Configurable
@TeleOp(name = "Loop Check", group = "Test")
public class LoopCheck extends CommandOpMode {
    public static double PATH_IN = 24;
    // Same as the far-start Auto
    public static double MAX_SPEED_IN_PER_S = 40;
    public static long PATH_TIMEOUT_MS = 4000;
    public static long FPS_PERIOD_MS = 2000;
    public static long LOG_PERIOD_MS = 40;

    // The first one is the default (Vision.POLL_RATE_HZ)
    private static final int[] POLL_RATES = {50, 100, 30};

    private Drivetrain drivetrain;
    private Vision vision;
    private Hives hives;
    private HiveAim aim;
    private List<LynxModule> hubs;

    private boolean cameraOn = true;
    private boolean processingOn = true;
    private boolean logOn = true;
    private boolean bulkOn = true;
    private int pollIndex = 0;

    private boolean driving = false;
    private boolean goingOut = true;
    private Pose home;
    private Command leg;

    private final CsvLog log = new CsvLog();
    private final ElapsedTime runTime = new ElapsedTime();
    private final ElapsedTime fpsTimer = new ElapsedTime();
    private long lastLogMs = Long.MIN_VALUE / 2;
    private double cameraFps = 0;
    private double fpsRequestMs = 0;
    private long lastLoopNs = 0;

    // Parts of the loop, in order: total, scheduler (commands), Pedro update, camera, HIVEs, aim, log, telemetry
    private final Stat total = new Stat(), commands = new Stat(), pedro = new Stat(), camera = new Stat(),
            hiveStat = new Stat(), aimStat = new Stat(), logStat = new Stat(), screen = new Stat();
    private final Stat[] all = {total, commands, pedro, camera, hiveStat, aimStat, logStat, screen};
    private final ElapsedTime statWindow = new ElapsedTime();

    // Gamepad edges (the buttons are read by hand: no subsystem is registered, see initialize())
    private boolean lastA, lastB, lastX, lastY, lastUp, lastDown, lastLeft;

    @Override
    public void initialize() {
        reset();
        telemetry = TelemetryUtil.withPanels(telemetry);
        hubs = hardwareMap.getAll(LynxModule.class);
        setBulk(true);
        drivetrain = new Drivetrain(hardwareMap);
        vision = new Vision(hardwareMap);
        hives = new Hives(vision);
        aim = new HiveAim(vision, hives);
        aim.setAlliance(Alliance.RED);
        // run() calls each periodic() itself, so each one can be timed alone. Subsystems register themselves when
        // created (SolversLib's SubsystemBase): take them out, or each periodic() runs twice (the first test, 2026-11-22)
        CommandScheduler.getInstance().unregisterSubsystem(drivetrain, vision, hives, aim);
        if (MAX_SPEED_IN_PER_S > 0) speedLimit().set(MAX_SPEED_IN_PER_S);
        telemetry.addLine("Face the HIVE tags with " + (int) PATH_IN + " in. clear in front, then START and A");
        telemetry.update();
    }

    @Override
    public void preRun() {
        drivetrain.setPose(new Pose(0, 0, 0));
        vision.setPollRateHz(POLL_RATES[pollIndex]);
        vision.start();
        log.open("loopcheck", "t_ms,camera,poll_hz,processing,log,bulk,driving,tags,camera_fps,loop_ms,commands_ms,"
                + "pedro_ms,camera_ms,hives_ms,aim_ms,log_ms,telemetry_ms,speed_in_s");
        runTime.reset();
        fpsTimer.reset();
        statWindow.reset();
        lastLoopNs = System.nanoTime();
    }

    @Override
    public void run() {
        long start = System.nanoTime();
        total.add((start - lastLoopNs) / 1e6);
        lastLoopNs = start;
        if (bulkOn) for (LynxModule hub : hubs) hub.clearBulkCache();

        buttons();
        drive();

        long t = System.nanoTime();
        super.run();
        t = time(commands, t);
        drivetrain.periodic();
        t = time(pedro, t);
        vision.periodic();
        t = time(camera, t);
        if (processingOn) hives.periodic();
        t = time(hiveStat, t);
        if (processingOn) aim.periodic();
        t = time(aimStat, t);
        if (logOn) logRow();
        t = time(logStat, t);

        if (cameraOn && fpsTimer.milliseconds() >= FPS_PERIOD_MS) {
            fpsTimer.reset();
            long f = System.nanoTime();
            try {
                cameraFps = vision.requestCameraFps();
            } catch (RuntimeException e) {
                cameraFps = -1;
            }
            long requestNs = System.nanoTime() - f;
            fpsRequestMs = requestNs / 1e6;
            // The frame-rate request is shown on its own: left out of the parts and of the whole loop
            t = System.nanoTime();
            lastLoopNs += requestNs;
        }

        showScreen();
        time(screen, t);

        if (statWindow.milliseconds() >= 1000) {
            statWindow.reset();
            for (Stat s : all) s.publish();
        }
    }

    /** A = drive back and forth, B = stop, X = camera, Y = our tag processing, D-pad: ask rate, log, bulk reads. */
    private void buttons() {
        if (gamepad1.a && !lastA && !driving) {
            driving = true;
            goingOut = true;
            home = drivetrain.pose();
        }
        if (gamepad1.b && !lastB) stopDriving();
        if (gamepad1.x && !lastX) {
            cameraOn = !cameraOn;
            if (cameraOn) vision.start();
            else vision.stop();
        }
        if (gamepad1.y && !lastY) processingOn = !processingOn;
        if (gamepad1.dpad_up && !lastUp) {
            pollIndex = (pollIndex + 1) % POLL_RATES.length;
            vision.setPollRateHz(POLL_RATES[pollIndex]);
        }
        if (gamepad1.dpad_down && !lastDown) logOn = !logOn;
        if (gamepad1.dpad_left && !lastLeft) setBulk(!bulkOn);
        lastA = gamepad1.a;
        lastB = gamepad1.b;
        lastX = gamepad1.x;
        lastY = gamepad1.y;
        lastUp = gamepad1.dpad_up;
        lastDown = gamepad1.dpad_down;
        lastLeft = gamepad1.dpad_left;
    }

    /** Starts the next leg (out or back) when the last one is over. */
    private void drive() {
        if (!driving || (leg != null && leg.isScheduled())) return;
        Pose here = drivetrain.pose();
        double h = home.heading();
        Pose end = goingOut
                ? new Pose(home.x() + PATH_IN * Math.cos(h), home.y() + PATH_IN * Math.sin(h), h)
                : home;
        goingOut = !goingOut;
        leg = new FollowPathCommand(drivetrain.follower, line(here, end).linear(here, end)).withTimeout(PATH_TIMEOUT_MS);
        schedule(leg);
    }

    private void stopDriving() {
        driving = false;
        if (leg != null) leg.cancel();
        leg = null;
        drivetrain.driveFieldCentric(0, 0, 0);
    }

    private void setBulk(boolean on) {
        bulkOn = on;
        for (LynxModule hub : hubs) {
            hub.setBulkCachingMode(on ? LynxModule.BulkCachingMode.MANUAL : LynxModule.BulkCachingMode.OFF);
        }
    }

    private void logRow() {
        long now = (long) runTime.milliseconds();
        if (now - lastLogMs < LOG_PERIOD_MS) return;
        lastLogMs = now;
        log.row(now, cameraOn, POLL_RATES[pollIndex], processingOn, logOn, bulkOn, driving, vision.getTags().size(),
                String.format("%.0f", cameraFps), ms(total.last), ms(commands.last), ms(pedro.last), ms(camera.last),
                ms(hiveStat.last), ms(aimStat.last), ms(logStat.last), ms(screen.last),
                String.format("%.1f", Math.hypot(drivetrain.follower.velocity().vx, drivetrain.follower.velocity().vy)));
    }

    private void showScreen() {
        telemetry.addLine("A = drive back and forth, B = stop" + (driving ? "   (DRIVING)" : ""));
        telemetry.addData("X  camera", onOff(cameraOn) + "   (" + vision.getTags().size() + " tags in view)");
        telemetry.addData("D-pad up  ask rate", POLL_RATES[pollIndex] + " per second");
        telemetry.addData("Y  our tag processing", onOff(processingOn));
        telemetry.addData("D-pad down  log", onOff(logOn) + "   (" + log.getStatus() + ")");
        telemetry.addData("D-pad left  bulk reads", onOff(bulkOn));
        telemetry.addData("Camera frame rate", cameraOn
                ? String.format("%.0f per second (asking took %.0f ms)", cameraFps, fpsRequestMs) : "camera off");
        telemetry.addLine("Last second, average / worst (ms):");
        telemetry.addLine(row("Whole loop", total));
        telemetry.addLine(row("  Commands", commands));
        telemetry.addLine(row("  Pedro + Pinpoint", pedro));
        telemetry.addLine(row("  Camera result", camera));
        telemetry.addLine(row("  HIVE check", hiveStat));
        telemetry.addLine(row("  Aim", aimStat));
        telemetry.addLine(row("  Log", logStat));
        telemetry.addLine(row("  Screen", screen));
        telemetry.update();
    }

    private static String row(String name, Stat s) {
        return String.format("%s: %.1f / %.1f", name, s.shownAverage, s.shownWorst);
    }

    private static String onOff(boolean on) {
        return on ? "ON" : "off";
    }

    private static String ms(double value) {
        return String.format("%.1f", value);
    }

    /** Adds the time since t to the part and returns now. */
    private static long time(Stat stat, long t) {
        long now = System.nanoTime();
        stat.add((now - t) / 1e6);
        return now;
    }

    private ConfigVar<Double> speedLimit() {
        return ((Foresight) drivetrain.follower.algorithm()).config.maxVelocityConstraint;
    }

    @Override
    public void end() {
        stopDriving();
        log.close();
        vision.stop();
        vision.setPollRateHz(POLL_RATES[0]);
        // The limit lives in a shared setting: give it back so the next OpMode is not slowed
        speedLimit().set(ForesightConfig.Constraint.NONE);
    }

    /** Times of one part of the loop: the last one, and the average and worst over the last second. */
    private static class Stat {
        double last, sum, worst, shownAverage, shownWorst;
        int count;

        void add(double ms) {
            last = ms;
            sum += ms;
            count++;
            worst = Math.max(worst, ms);
        }

        void publish() {
            shownAverage = count > 0 ? sum / count : 0;
            shownWorst = worst;
            sum = 0;
            worst = 0;
            count = 0;
        }
    }
}
