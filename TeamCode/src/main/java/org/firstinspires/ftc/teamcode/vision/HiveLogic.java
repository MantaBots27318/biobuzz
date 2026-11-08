package org.firstinspires.ftc.teamcode.vision;

import java.util.Arrays;
import java.util.List;

/**
 * The maths behind "is this CELL up?", with no hardware so it can be checked on its own.
 * Camera frame: x right, y down, z forward, inches.
 */
public final class HiveLogic {
    private HiveLogic() {}

    /** What one loop's readings say about a cell, before the hold time is applied. */
    public enum Raw { UP, DOWN, MOVING, NONE }

    /** One cluster as seen by the camera: median of its tags. */
    public static final class Reading {
        /** Height of the tags above the camera, along the true vertical. */
        public final double heightAboveCameraIn;
        /** Angle of the tags' face from straight down: about 30 in both resting positions. */
        public final double angleFromDownDeg;
        public final int tags;

        Reading(double heightAboveCameraIn, double angleFromDownDeg, int tags) {
            this.heightAboveCameraIn = heightAboveCameraIn;
            this.angleFromDownDeg = angleFromDownDeg;
            this.tags = tags;
        }
    }

    /** The result for the two cells of one HIVE. */
    public static final class Verdict {
        public final Raw cellA, cellB;
        public final String rule;

        Verdict(Raw cellA, Raw cellB, String rule) {
            this.cellA = cellA;
            this.cellB = cellB;
            this.rule = rule;
        }
    }

    /**
     * @param tags each is {x, y, z, rollDeg, pitchDeg, yawDeg} of one tag in the camera frame
     * @param cameraTiltDeg how far the camera points above level
     * @return the medians over the tags, or null when there is no tag
     */
    public static Reading read(List<double[]> tags, double cameraTiltDeg) {
        if (tags.isEmpty()) return null;
        double t = Math.toRadians(cameraTiltDeg);
        double[] heights = new double[tags.size()];
        double[] angles = new double[tags.size()];
        for (int i = 0; i < tags.size(); i++) {
            double[] tag = tags.get(i);
            heights[i] = -tag[1] * Math.cos(t) + tag[2] * Math.sin(t);
            double[] n = TagMap.tagNormalInCamera(tag[3], tag[4], tag[5]);
            // World "up" seen from the camera frame
            double nUp = n[1] * -Math.cos(t) + n[2] * Math.sin(t);
            angles[i] = Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, -nUp))));
        }
        return new Reading(median(heights), median(angles), tags.size());
    }

    /**
     * Decides the state of the two cells of one HIVE, in this order: tipping (angle off), the higher cell
     * when both are in view, then the height threshold when only one is.
     * Pass null for a cell that is not in view.
     */
    public static Verdict classify(Reading a, Reading b, double cameraHeightIn, double thresholdIn,
                                   double minHeightDifferenceIn, double angleDeg, double toleranceDeg) {
        if (a == null && b == null) return new Verdict(Raw.NONE, Raw.NONE, "no tags in view");

        boolean aOff = a != null && Math.abs(a.angleFromDownDeg - angleDeg) > toleranceDeg;
        boolean bOff = b != null && Math.abs(b.angleFromDownDeg - angleDeg) > toleranceDeg;
        if (aOff || bOff) {
            return new Verdict(a == null ? Raw.NONE : Raw.MOVING, b == null ? Raw.NONE : Raw.MOVING,
                    "angle is not " + Math.round(angleDeg) + " deg: tipping");
        }

        if (a != null && b != null) {
            double diff = a.heightAboveCameraIn - b.heightAboveCameraIn;
            if (Math.abs(diff) < minHeightDifferenceIn) {
                return new Verdict(Raw.MOVING, Raw.MOVING, "both cells at the same height");
            }
            return diff > 0
                    ? new Verdict(Raw.UP, Raw.DOWN, "higher cell is up (both in view)")
                    : new Verdict(Raw.DOWN, Raw.UP, "higher cell is up (both in view)");
        }

        Reading seen = a != null ? a : b;
        Raw raw = seen.heightAboveCameraIn + cameraHeightIn >= thresholdIn ? Raw.UP : Raw.DOWN;
        return a != null ? new Verdict(raw, Raw.NONE, "height threshold (one cell in view)")
                : new Verdict(Raw.NONE, raw, "height threshold (one cell in view)");
    }

    static double median(double[] values) {
        double[] sorted = Arrays.copyOf(values, values.length);
        Arrays.sort(sorted);
        int n = sorted.length;
        return n % 2 == 1 ? sorted[n / 2] : (sorted[n / 2 - 1] + sorted[n / 2]) / 2;
    }
}
