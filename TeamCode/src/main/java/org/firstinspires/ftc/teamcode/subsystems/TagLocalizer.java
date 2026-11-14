package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.math.Pose;
import com.seattlesolvers.solverslib.command.SubsystemBase;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.LocalizerLogic;
import org.firstinspires.ftc.teamcode.vision.TagMap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;

/**
 * Nudges the Pinpoint's x and y toward what the AprilTags say. The heading is never changed. Each new picture
 * is compared with the Pinpoint's pose from the moment it was taken (the camera's delay), and the difference
 * is added to the current pose with a gain. Needs Vision and Hives to be updated first.
 *
 * Not yet tested on the robot: the gains and limits are placeholders to tune.
 */
@Configurable
public class TagLocalizer extends SubsystemBase {
    public static boolean ENABLED = true;

    // Where the camera sits relative to the robot's reference point (the Pinpoint's tracking point), in the robot's frame
    public static double CAMERA_FORWARD_IN = 6;
    public static double CAMERA_LEFT_IN = 0;
    // Which way the camera points compared with the robot's front: only used for the heading information
    public static double CAMERA_YAW_DEG = 0;

    public static double BASE_GAIN = 0.15;
    public static int TAGS_FOR_FULL_GAIN = 3;
    public static double NEAR_DISTANCE_IN = 40;
    public static double MAX_DISTANCE_IN = 100;
    public static double SLOW_SPEED_IN_PER_S = 6;
    public static double MAX_SPEED_IN_PER_S = 30;
    public static double MAX_TURN_DEG_PER_S = 90;
    // How far the camera height computed from a tag may be from the real mounting height (Hives.CAMERA_HEIGHT_IN)
    public static double HEIGHT_TOLERANCE_IN = 6;
    public static double SNAP_DISTANCE_IN = 10;
    public static int SNAP_COUNT = 4;
    public static double SNAP_CONSISTENCY_IN = 4;
    public static double MAX_LATENCY_MS = 300;
    // Added to the delay the Limelight reports, if it turns out to be short
    public static double EXTRA_LATENCY_MS = 0;
    public static long HISTORY_MS = 1000;

    // The TeleOp start spot, used when no Auto just ran: blue-side coordinates, turned for red like every pose
    public static boolean TELEOP_START_ENABLED = false;
    public static double TELEOP_START_X_IN = 120;
    public static double TELEOP_START_Y_IN = 24;
    public static double TELEOP_START_HEADING_DEG = 180;

    private final Drivetrain drivetrain;
    private final Vision vision;
    private final Hives hives;
    private final LocalizerLogic.SnapTracker snap = new LocalizerLogic.SnapTracker();
    // Pinpoint poses with their times: {timeMs, x, y, heading}
    private final List<double[]> history = new ArrayList<>();
    private LongSupplier clockMs = () -> System.nanoTime() / 1_000_000L;

    private long lastFrame = -1;
    private boolean poseKnown = true;
    private String status = "waiting for a picture";
    private int tagsUsed;
    private double measuredX, measuredY, lastErrorIn, lastGain, headingDisagreementDeg;

    public TagLocalizer(Drivetrain drivetrain, Vision vision, Hives hives) {
        this.drivetrain = drivetrain;
        this.vision = vision;
        this.hives = hives;
    }

    /** For checks that need to control time. */
    public void setClock(LongSupplier clockMs) {
        this.clockMs = clockMs;
    }

    /** False when the robot does not know where it is: the first good picture then sets the position. */
    public void setPoseKnown(boolean known) {
        poseKnown = known;
        snap.reset();
    }

    public boolean isPoseKnown() {
        return poseKnown;
    }

    public boolean hasTeleOpStart() {
        return TELEOP_START_ENABLED;
    }

    /** The TeleOp start pose for this alliance. */
    public Pose teleOpStartPose(Alliance alliance) {
        return alliance.poses().of(TELEOP_START_X_IN, TELEOP_START_Y_IN, TELEOP_START_HEADING_DEG);
    }

    @Override
    public void periodic() {
        long now = clockMs.getAsLong();
        Pose pose = drivetrain.pose();
        history.add(new double[]{now, pose.x(), pose.y(), pose.heading()});
        while (history.size() > 1 && now - history.get(0)[0] > HISTORY_MS) history.remove(0);

        if (vision.getFrameId() == lastFrame) return;
        lastFrame = vision.getFrameId();
        correct(now);
    }

    private void correct(long now) {
        if (!ENABLED) {
            status = "off";
            return;
        }
        double latency = vision.getLatencyMs() + EXTRA_LATENCY_MS;
        if (latency > MAX_LATENCY_MS) {
            status = String.format("picture too old (%.0f ms)", latency);
            return;
        }
        double captureMs = now - latency;
        double[] at = poseAt(captureMs);
        if (at == null) {
            status = "no pose history yet";
            return;
        }

        // Tags of a HIVE that is settled; the map needs to know which cell is up
        Hives.Side blueSide = hives.getSideUp(Hives.Hive.BLUE);
        Hives.Side redSide = hives.getSideUp(Hives.Hive.RED);
        List<LocalizerLogic.TagInput> inputs = new ArrayList<>();
        for (Vision.Tag tag : vision.getTags()) {
            boolean blue = tag.id >= 38 && tag.id <= 45;
            Hives.Side side = blue ? blueSide : redSide;
            if (side == Hives.Side.UNKNOWN) continue;
            TagMap.TagPose pose = TagMap.pose(tag.id, blue && side == Hives.Side.FAR, !blue && side == Hives.Side.FAR);
            if (pose != null) {
                inputs.add(new LocalizerLogic.TagInput(pose, tag.xIn, tag.yIn, tag.zIn,
                        tag.rollDeg, tag.pitchDeg, tag.yawDeg));
            }
        }
        if (inputs.isEmpty()) {
            tagsUsed = 0;
            status = "no tag of a settled HIVE in view";
            return;
        }

        LocalizerLogic.Estimate est = LocalizerLogic.estimate(inputs, Hives.CAMERA_HEIGHT_IN,
                HEIGHT_TOLERANCE_IN, MAX_DISTANCE_IN);
        tagsUsed = est.used;
        if (est.used == 0) {
            status = est.rejectedHeight > 0 ? "tags rejected: camera height from them is off" : "tags too far";
            return;
        }

        // How fast the robot moved around the time of the picture
        double[] before = poseAt(captureMs - 50);
        double[] after = poseAt(captureMs + 50);
        if (after[0] - before[0] < 40) {
            // Too little history to tell how fast the robot moves (just after START)
            status = "waiting for pose history";
            return;
        }
        double dt = (after[0] - before[0]) / 1000.0;
        double speed = Math.hypot(after[1] - before[1], after[2] - before[2]) / dt;
        double turnRate = Math.abs(Math.toDegrees(angleDiff(after[3], before[3]))) / dt;
        if (speed > MAX_SPEED_IN_PER_S || turnRate > MAX_TURN_DEG_PER_S) {
            status = String.format("robot moving too fast (%.0f in/s, %.0f deg/s)", speed, turnRate);
            return;
        }

        double[] robot = LocalizerLogic.robotFromCamera(est.camX, est.camY, at[3], CAMERA_FORWARD_IN, CAMERA_LEFT_IN);
        measuredX = robot[0];
        measuredY = robot[1];
        double errX = measuredX - at[1];
        double errY = measuredY - at[2];
        lastErrorIn = Math.hypot(errX, errY);
        headingDisagreementDeg = Math.toDegrees(angleDiff(Math.toRadians(est.headingDeg),
                at[3] + Math.toRadians(CAMERA_YAW_DEG)));

        double gain;
        if (!poseKnown) {
            gain = 1;
            poseKnown = true;
            status = String.format("position set from %d tag(s)", est.used);
        } else if (lastErrorIn > SNAP_DISTANCE_IN) {
            // A big disagreement is never pulled in slowly: either several pictures in a row confirm it (a bump)
            // and the position jumps, or it is ignored (a wrong reading)
            if (!snap.update(lastErrorIn, measuredX, measuredY, SNAP_DISTANCE_IN, SNAP_COUNT, SNAP_CONSISTENCY_IN)) {
                lastGain = 0;
                status = String.format("large disagreement (%.1f in): waiting to confirm %d/%d",
                        lastErrorIn, snap.getCount(), SNAP_COUNT);
                return;
            }
            gain = 1;
            status = String.format("bump: jumped by %.1f in using %d tag(s)", lastErrorIn, est.used);
        } else {
            snap.reset();
            gain = LocalizerLogic.gain(BASE_GAIN, est.used, TAGS_FOR_FULL_GAIN, est.distanceIn, NEAR_DISTANCE_IN,
                    MAX_DISTANCE_IN, speed, SLOW_SPEED_IN_PER_S, MAX_SPEED_IN_PER_S);
            status = String.format("corrected by %.1f in x %.2f using %d tag(s)", lastErrorIn, gain, est.used);
        }
        lastGain = gain;

        double dx = gain * errX;
        double dy = gain * errY;
        Pose current = drivetrain.pose();
        drivetrain.setPose(new Pose(current.x() + dx, current.y() + dy, current.heading()));
        // The stored poses must follow the same shift or the next picture would be compared with old ones
        for (double[] entry : history) {
            entry[1] += dx;
            entry[2] += dy;
        }
    }

    /** The Pinpoint's pose at a time (milliseconds on the same clock), by a straight line between samples. */
    private double[] poseAt(double timeMs) {
        if (history.isEmpty()) return null;
        double[] first = history.get(0);
        double[] last = history.get(history.size() - 1);
        if (timeMs <= first[0]) return first.clone();
        if (timeMs >= last[0]) return last.clone();
        for (int i = 1; i < history.size(); i++) {
            double[] b = history.get(i);
            if (b[0] >= timeMs) {
                double[] a = history.get(i - 1);
                double f = b[0] == a[0] ? 1 : (timeMs - a[0]) / (b[0] - a[0]);
                return new double[]{timeMs, a[1] + f * (b[1] - a[1]), a[2] + f * (b[2] - a[2]),
                        a[3] + f * angleDiff(b[3], a[3])};
            }
        }
        return last.clone();
    }

    /** a - b as an angle between -pi and pi. */
    private static double angleDiff(double a, double b) {
        double d = a - b;
        while (d > Math.PI) d -= 2 * Math.PI;
        while (d < -Math.PI) d += 2 * Math.PI;
        return d;
    }

    public String getStatus() {
        return status;
    }

    public int getTagsUsed() {
        return tagsUsed;
    }

    public double getMeasuredX() {
        return measuredX;
    }

    public double getMeasuredY() {
        return measuredY;
    }

    public double getLastErrorIn() {
        return lastErrorIn;
    }

    public double getLastGain() {
        return lastGain;
    }

    public int getSnapCount() {
        return snap.getCount();
    }

    /** Camera heading from the tags minus the Pinpoint's heading (plus the camera's yaw): information only. */
    public double getHeadingDisagreementDeg() {
        return headingDisagreementDeg;
    }
}
