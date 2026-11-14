package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.seattlesolvers.solverslib.command.SubsystemBase;

import org.firstinspires.ftc.teamcode.vision.HiveLogic;

import java.util.ArrayList;
import java.util.List;

/**
 * Tells which CELL of each HIVE is up, from the AprilTags the Limelight sees. We only shoot into a CELL
 * that is up and has stopped moving. See HiveLogic for the rules. Needs Vision to be updated first.
 *
 * The Limelight's angle readings are not yet verified on the robot for this use: tune at HQ.
 */
@Configurable
public class Hives extends SubsystemBase {
    // How the Limelight is mounted: measured on the test setup, to redo on the final robot
    public static double CAMERA_TILT_DEG = 28;
    public static double CAMERA_HEIGHT_IN = 14;
    // Tag centers are about 49.7 in. high when a CELL is up and 35.9 in. when it is down
    public static double UP_HEIGHT_THRESHOLD_IN = 43;
    public static double MIN_HEIGHT_DIFFERENCE_IN = 6;
    // Tags are tilted 30 deg from straight down in both resting positions, about 0 deg halfway through a tip
    public static double TAG_ANGLE_DEG = 30;
    public static double TAG_ANGLE_TOLERANCE_DEG = 12;
    public static long HOLD_MS = 150;
    public static long NOT_SEEN_MS = 400;

    public enum Hive { BLUE, RED }

    /** The 4 CELLS that carry tags. "Far" = opposite the audience. */
    public enum Cluster {
        BLUE_FAR(Hive.BLUE, true, 42),
        BLUE_AUDIENCE(Hive.BLUE, false, 38),
        RED_FAR(Hive.RED, true, 30),
        RED_AUDIENCE(Hive.RED, false, 34);

        public final Hive hive;
        public final boolean far;
        final int firstId;

        Cluster(Hive hive, boolean far, int firstId) {
            this.hive = hive;
            this.far = far;
            this.firstId = firstId;
        }

        boolean contains(int id) {
            return id >= firstId && id < firstId + 4;
        }
    }

    public enum CellState { UP, DOWN, MOVING, NOT_SEEN }

    public enum Side { FAR, AUDIENCE, UNKNOWN }

    private static class Slot {
        CellState state = CellState.NOT_SEEN;
        CellState lastKnown = CellState.NOT_SEEN;
        HiveLogic.Raw pending = HiveLogic.Raw.NONE;
        long pendingSinceMs;
        long lastSeenMs = Long.MIN_VALUE / 2;
        String info = "";
    }

    private final Vision vision;
    private final Slot[] slots = new Slot[Cluster.values().length];
    private final String[] rules = new String[Hive.values().length];

    public Hives(Vision vision) {
        this.vision = vision;
        for (int i = 0; i < slots.length; i++) slots[i] = new Slot();
        for (int i = 0; i < rules.length; i++) rules[i] = "";
    }

    @Override
    public void periodic() {
        long nowMs = System.nanoTime() / 1_000_000L;
        HiveLogic.Reading[] readings = new HiveLogic.Reading[slots.length];
        for (Cluster cluster : Cluster.values()) {
            List<double[]> tags = new ArrayList<>();
            for (Vision.Tag tag : vision.getTags()) {
                if (cluster.contains(tag.id)) {
                    tags.add(new double[]{tag.xIn, tag.yIn, tag.zIn, tag.rollDeg, tag.pitchDeg, tag.yawDeg});
                }
            }
            readings[cluster.ordinal()] = HiveLogic.read(tags, CAMERA_TILT_DEG);
        }

        for (Hive hive : Hive.values()) {
            Cluster far = hive == Hive.BLUE ? Cluster.BLUE_FAR : Cluster.RED_FAR;
            Cluster audience = hive == Hive.BLUE ? Cluster.BLUE_AUDIENCE : Cluster.RED_AUDIENCE;
            HiveLogic.Verdict verdict = HiveLogic.classify(readings[far.ordinal()], readings[audience.ordinal()],
                    CAMERA_HEIGHT_IN, UP_HEIGHT_THRESHOLD_IN, MIN_HEIGHT_DIFFERENCE_IN,
                    TAG_ANGLE_DEG, TAG_ANGLE_TOLERANCE_DEG);
            rules[hive.ordinal()] = verdict.rule;
            update(far, verdict.cellA, readings[far.ordinal()], nowMs);
            update(audience, verdict.cellB, readings[audience.ordinal()], nowMs);
        }
    }

    private void update(Cluster cluster, HiveLogic.Raw raw, HiveLogic.Reading reading, long nowMs) {
        Slot slot = slots[cluster.ordinal()];
        if (raw == HiveLogic.Raw.NONE) {
            if (nowMs - slot.lastSeenMs >= NOT_SEEN_MS) {
                slot.state = CellState.NOT_SEEN;
                slot.pending = HiveLogic.Raw.NONE;
                slot.info = "not in view";
            }
            return;
        }
        slot.lastSeenMs = nowMs;
        slot.info = String.format("height %.1f in above the camera, angle %.0f deg, %d tag(s)",
                reading.heightAboveCameraIn, reading.angleFromDownDeg, reading.tags);
        if (raw != slot.pending) {
            slot.pending = raw;
            slot.pendingSinceMs = nowMs;
        }
        // Stopping is immediate (never shoot into a HIVE that starts to tip); anything else must hold
        if (raw == HiveLogic.Raw.MOVING || nowMs - slot.pendingSinceMs >= HOLD_MS) {
            slot.state = raw == HiveLogic.Raw.UP ? CellState.UP
                    : raw == HiveLogic.Raw.DOWN ? CellState.DOWN : CellState.MOVING;
            if (slot.state == CellState.UP || slot.state == CellState.DOWN) slot.lastKnown = slot.state;
        }
    }

    public CellState getState(Cluster cluster) {
        return slots[cluster.ordinal()].state;
    }

    /** The last settled state (UP or DOWN) seen for this CELL; NOT_SEEN if it never was. */
    public CellState getLastKnownState(Cluster cluster) {
        return slots[cluster.ordinal()].lastKnown;
    }

    /** True only when this CELL is up, has stopped moving, and was seen a moment ago. */
    public boolean isCellUp(Cluster cluster) {
        return getState(cluster) == CellState.UP;
    }

    /** Which CELL of this HIVE is up now: one that is up, or the other one when a CELL is settled down. */
    public Side getSideUp(Hive hive) {
        return sideFrom(getState(farOf(hive)), getState(audienceOf(hive)));
    }

    /** The same, from the last settled states (kept when the tags are lost). For aiming. */
    public Side getLastKnownSideUp(Hive hive) {
        return sideFrom(getLastKnownState(farOf(hive)), getLastKnownState(audienceOf(hive)));
    }

    public String getInfo(Cluster cluster) {
        return slots[cluster.ordinal()].info;
    }

    public String getRule(Hive hive) {
        return rules[hive.ordinal()];
    }

    private static Cluster farOf(Hive hive) {
        return hive == Hive.BLUE ? Cluster.BLUE_FAR : Cluster.RED_FAR;
    }

    private static Cluster audienceOf(Hive hive) {
        return hive == Hive.BLUE ? Cluster.BLUE_AUDIENCE : Cluster.RED_AUDIENCE;
    }

    private static Side sideFrom(CellState far, CellState audience) {
        if (far == CellState.UP || audience == CellState.DOWN) return Side.FAR;
        if (audience == CellState.UP || far == CellState.DOWN) return Side.AUDIENCE;
        return Side.UNKNOWN;
    }
}
