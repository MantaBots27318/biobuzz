package org.firstinspires.ftc.teamcode.vision;

import java.util.List;

/**
 * The maths behind correcting the robot's position from AprilTags, with no hardware so it can be checked on
 * its own. Field frame: inches, heading 0 toward +x, counter-clockwise positive.
 */
public final class LocalizerLogic {
    private LocalizerLogic() {}

    /** One tag as read by the camera, with the pose the map says it has. */
    public static final class TagInput {
        public final TagMap.TagPose pose;
        public final double x, y, z, rollDeg, pitchDeg, yawDeg;

        public TagInput(TagMap.TagPose pose, double x, double y, double z,
                        double rollDeg, double pitchDeg, double yawDeg) {
            this.pose = pose;
            this.x = x;
            this.y = y;
            this.z = z;
            this.rollDeg = rollDeg;
            this.pitchDeg = pitchDeg;
            this.yawDeg = yawDeg;
        }
    }

    /** The camera's position on the field, from the tags that passed the checks. */
    public static final class Estimate {
        public final double camX, camY;
        /** Mean distance along the floor from the camera to the tags used. */
        public final double distanceIn;
        /** Camera heading from the tags, in degrees: for information only, never used to correct. */
        public final double headingDeg;
        public final int used, rejectedHeight, rejectedFar;

        Estimate(double camX, double camY, double distanceIn, double headingDeg,
                 int used, int rejectedHeight, int rejectedFar) {
            this.camX = camX;
            this.camY = camY;
            this.distanceIn = distanceIn;
            this.headingDeg = headingDeg;
            this.used = used;
            this.rejectedHeight = rejectedHeight;
            this.rejectedFar = rejectedFar;
        }
    }

    /**
     * The camera's position from several tags: each tag gives one, a tag is thrown out when the camera height
     * it implies is not the real one (a wrong reading) or when it is too far, and the median of the rest is kept.
     * With no usable tag, used is 0 and the other numbers mean nothing.
     */
    public static Estimate estimate(List<TagInput> tags, double cameraHeightIn, double heightToleranceIn,
                                    double maxDistanceIn) {
        double[] xs = new double[tags.size()];
        double[] ys = new double[tags.size()];
        double distSum = 0, sinSum = 0, cosSum = 0;
        int used = 0, badHeight = 0, tooFar = 0;
        for (TagInput t : tags) {
            TagMap.CameraPose cam = TagMap.cameraFromTag(t.pose, t.x, t.y, t.z, t.rollDeg, t.pitchDeg, t.yawDeg);
            if (Math.abs(cam.z - cameraHeightIn) > heightToleranceIn) {
                badHeight++;
                continue;
            }
            double dist = Math.hypot(cam.x - t.pose.x, cam.y - t.pose.y);
            if (dist > maxDistanceIn) {
                tooFar++;
                continue;
            }
            xs[used] = cam.x;
            ys[used] = cam.y;
            distSum += dist;
            sinSum += Math.sin(Math.toRadians(cam.headingDeg));
            cosSum += Math.cos(Math.toRadians(cam.headingDeg));
            used++;
        }
        if (used == 0) return new Estimate(0, 0, 0, 0, 0, badHeight, tooFar);
        double[] x = java.util.Arrays.copyOf(xs, used);
        double[] y = java.util.Arrays.copyOf(ys, used);
        return new Estimate(HiveLogic.median(x), HiveLogic.median(y), distSum / used,
                Math.toDegrees(Math.atan2(sinSum, cosSum)), used, badHeight, tooFar);
    }

    /**
     * The robot's reference point from the camera's position. The camera sits forwardIn ahead of and leftIn to
     * the left of the reference point, in the robot's own frame.
     */
    public static double[] robotFromCamera(double camX, double camY, double headingRad,
                                           double forwardIn, double leftIn) {
        double dx = forwardIn * Math.cos(headingRad) - leftIn * Math.sin(headingRad);
        double dy = forwardIn * Math.sin(headingRad) + leftIn * Math.cos(headingRad);
        return new double[]{camX - dx, camY - dy};
    }

    /** 1 up to "full", 0 from "zero" on, a straight line between. */
    public static double ramp(double value, double full, double zero) {
        if (value <= full) return 1;
        if (value >= zero) return 0;
        return (zero - value) / (zero - full);
    }

    /** How far to pull toward the tags: strong for close tags, several tags and a slow robot. */
    public static double gain(double baseGain, int tags, int tagsForFullGain, double distanceIn, double nearIn,
                              double maxDistanceIn, double speedInPerS, double slowInPerS, double maxInPerS) {
        double tagFactor = Math.min(1.0, (double) tags / Math.max(1, tagsForFullGain));
        return baseGain * tagFactor * ramp(distanceIn, nearIn, maxDistanceIn) * ramp(speedInPerS, slowInPerS, maxInPerS);
    }

    /**
     * Spots a bump: several readings in a row that disagree with the Pinpoint by more than snapDistance and agree
     * with each other.
     */
    public static final class SnapTracker {
        private int count;
        private double lastX, lastY;

        /** Returns true when the position should jump to the measured one. */
        public boolean update(double errorIn, double measuredX, double measuredY,
                              double snapDistanceIn, int snapCount, double consistencyIn) {
            if (errorIn <= snapDistanceIn) {
                count = 0;
                return false;
            }
            if (count > 0 && Math.hypot(measuredX - lastX, measuredY - lastY) > consistencyIn) count = 0;
            count++;
            lastX = measuredX;
            lastY = measuredY;
            if (count >= snapCount) {
                count = 0;
                return true;
            }
            return false;
        }

        public int getCount() {
            return count;
        }

        public void reset() {
            count = 0;
        }
    }
}
