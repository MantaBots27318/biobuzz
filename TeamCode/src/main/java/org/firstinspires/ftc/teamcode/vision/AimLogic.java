package org.firstinspires.ftc.teamcode.vision;

import java.util.List;

/**
 * The maths behind aiming at tags, with no hardware so it can be checked on its own.
 * Camera frame: x right, y down, z forward, inches.
 */
public final class AimLogic {
    private AimLogic() {}

    /**
     * The angle to a tag as seen from above: positive when the tag is to the right of the camera's line of
     * sight. The camera looks up by tiltDeg, so the tag's distance along the floor is not its z.
     */
    public static double bearingFromAboveDeg(double xIn, double yIn, double zIn, double cameraTiltDeg) {
        double t = Math.toRadians(cameraTiltDeg);
        return Math.toDegrees(Math.atan2(xIn, zIn * Math.cos(t) + yIn * Math.sin(t)));
    }

    /** Median bearing over tags given as {x, y, z}, or NaN when there is none. */
    public static double medianBearingDeg(List<double[]> tags, double cameraTiltDeg) {
        if (tags.isEmpty()) return Double.NaN;
        double[] bearings = new double[tags.size()];
        for (int i = 0; i < bearings.length; i++) {
            double[] tag = tags.get(i);
            bearings[i] = bearingFromAboveDeg(tag[0], tag[1], tag[2], cameraTiltDeg);
        }
        return HiveLogic.median(bearings);
    }

    /**
     * The turn to send to the drivetrain. errorDeg is positive when the target is to the right, which needs a
     * clockwise turn, which is negative in Pedro (counter-clockwise is positive). Zero inside the tolerance.
     */
    public static double turnPower(double errorDeg, double kP, double maxPower, double minPower, double toleranceDeg) {
        if (Math.abs(errorDeg) <= toleranceDeg) return 0;
        double power = Math.max(minPower, Math.min(maxPower, kP * Math.abs(errorDeg)));
        return -Math.signum(errorDeg) * power;
    }
}
