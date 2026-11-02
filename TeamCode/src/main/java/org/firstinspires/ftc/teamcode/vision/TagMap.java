package org.firstinspires.ftc.teamcode.vision;

import com.bylazar.configurables.annotations.Configurable;

/**
 * Where every AprilTag (IDs 30 to 45) is on the field, for each position of its HIVE, and the maths
 * that turns "where a tag appears to the camera" into "where the camera is on the field".
 *
 * Field frame (Pedro): inches, (0, 0) at the corner where the audience wall meets the red wall,
 * +x toward the blue wall, +y away from the audience, +z up from the tiles.
 *
 * Only one sticker was measured in the CAD (blue far cell, far cell up). The other cells and the
 * other position follow from the pivot, the bar tilt and the field's 180 degree symmetry.
 */
@Configurable
public class TagMap {
    public static double PIVOT_X_IN = 84.75;
    public static double PIVOT_Y_IN = 72;
    public static double PIVOT_Z_IN = 43.95;
    public static double BAR_TILT_DEG = 30;
    // Measured sticker center minus the pivot, blue HIVE, far cell up
    public static double STICKER_X_FROM_PIVOT_IN = -0.24;
    public static double STICKER_Y_FROM_PIVOT_IN = 12.62;
    public static double STICKER_Z_FROM_PIVOT_IN = 5.70;
    // First to last ID of a cluster, seen from below with the sticker's top away from the field center
    public static boolean IDS_LEFT_TO_RIGHT = true;
    // How the Limelight combines its 3 angles; flip only if every tag gives a wrong camera position
    public static boolean EULER_ZYX = true;

    private static final double FIELD_SIZE_IN = 144;
    // Centers of the 4 tags along a cluster's row, first to last ID (FTC SDK tag library)
    private static final double[] ROW_OFFSETS_IN = {-6.5, -2.75, 2.75, 6.5};

    /** One tag: its center, the direction it faces (toward whoever sees it) and its "up". */
    public static final class TagPose {
        public final int id;
        public final double x, y, z;
        public final double[] normal;
        public final double[] up;

        TagPose(int id, double x, double y, double z, double[] normal, double[] up) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.z = z;
            this.normal = normal;
            this.up = up;
        }
    }

    /** The camera on the field, with the direction it points (degrees, 0 = toward +x, tilt up positive). */
    public static final class CameraPose {
        public final double x, y, z;
        public final double headingDeg, tiltDeg;

        CameraPose(double x, double y, double z, double headingDeg, double tiltDeg) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.headingDeg = headingDeg;
            this.tiltDeg = tiltDeg;
        }
    }

    /**
     * @param blueFarCellUp true when the blue HIVE has its far cell up (far = opposite the audience)
     * @param redFarCellUp  the same for the red HIVE
     * @return the tag's pose, or null if the ID is not a BIOBUZZ tag
     */
    public static TagPose pose(int id, boolean blueFarCellUp, boolean redFarCellUp) {
        boolean red = id >= 30 && id <= 37;
        boolean blue = id >= 38 && id <= 45;
        if (!red && !blue) return null;

        // Work in blue-HIVE terms. A red cell is the blue cell on the opposite side, turned 180 degrees
        boolean blueFarCell;
        boolean farCellUp;
        int first;
        if (blue) {
            blueFarCell = id >= 42;
            first = blueFarCell ? 42 : 38;
            farCellUp = blueFarCellUp;
        } else {
            boolean redFarCell = id <= 33;
            blueFarCell = !redFarCell;
            first = redFarCell ? 30 : 34;
            farCellUp = !redFarCellUp;
        }

        double tilt = Math.toRadians(BAR_TILT_DEG);
        // In the (y, z) plane: along the bar (b) and across it (u), for the measured position
        double a = STICKER_Y_FROM_PIVOT_IN * Math.cos(tilt) + STICKER_Z_FROM_PIVOT_IN * Math.sin(tilt);
        double p = -STICKER_Y_FROM_PIVOT_IN * Math.sin(tilt) + STICKER_Z_FROM_PIVOT_IN * Math.cos(tilt);

        double theta = farCellUp ? tilt : -tilt;
        double side = blueFarCell ? 1 : -1;
        double by = Math.cos(theta), bz = Math.sin(theta);
        double uy = -Math.sin(theta), uz = Math.cos(theta);

        double cx = PIVOT_X_IN + STICKER_X_FROM_PIVOT_IN;
        double cy = PIVOT_Y_IN + side * a * by + p * uy;
        double cz = PIVOT_Z_IN + side * a * bz + p * uz;
        double[] normal = {0, -uy, -uz};
        double[] up = {0, side * by, side * bz};

        double[] right = cross(up, normal);
        double offset = ROW_OFFSETS_IN[id - first] * (IDS_LEFT_TO_RIGHT ? 1 : -1);
        double x = cx + offset * right[0];
        double y = cy + offset * right[1];
        double z = cz + offset * right[2];

        if (red) {
            x = FIELD_SIZE_IN - x;
            y = FIELD_SIZE_IN - y;
            normal = new double[]{-normal[0], -normal[1], normal[2]};
            up = new double[]{-up[0], -up[1], up[2]};
        }
        return new TagPose(id, x, y, z, normal, up);
    }

    /**
     * Where the camera is, given one tag the camera sees.
     *
     * @param tx, ty, tz the tag's center in the camera frame (x right, y down, z forward), inches
     * @param rollDeg, pitchDeg, yawDeg how the tag is turned relative to the camera, as the Limelight
     *                                  reports them (about the camera's x, y and z axes)
     */
    public static CameraPose cameraFromTag(TagPose tag, double tx, double ty, double tz,
                                           double rollDeg, double pitchDeg, double yawDeg) {
        double[][] rx = rotX(Math.toRadians(rollDeg));
        double[][] ry = rotY(Math.toRadians(pitchDeg));
        double[][] rz = rotZ(Math.toRadians(yawDeg));
        // Tag frame -> camera frame
        double[][] tagToCamera = EULER_ZYX ? mul(rz, mul(ry, rx)) : mul(rx, mul(ry, rz));

        // The tag's own frame as seen when looking at it: x right, y down, z into the tag
        double[] zt = {-tag.normal[0], -tag.normal[1], -tag.normal[2]};
        double[] yt = {-tag.up[0], -tag.up[1], -tag.up[2]};
        double[] xt = cross(yt, zt);
        double[][] tagToField = {
                {xt[0], yt[0], zt[0]},
                {xt[1], yt[1], zt[1]},
                {xt[2], yt[2], zt[2]}};

        double[][] cameraToField = mul(tagToField, transpose(tagToCamera));
        double[] offset = apply(cameraToField, new double[]{tx, ty, tz});
        double[] forward = {cameraToField[0][2], cameraToField[1][2], cameraToField[2][2]};
        return new CameraPose(tag.x - offset[0], tag.y - offset[1], tag.z - offset[2],
                Math.toDegrees(Math.atan2(forward[1], forward[0])),
                Math.toDegrees(Math.asin(Math.max(-1, Math.min(1, forward[2])))));
    }

    private static double[] cross(double[] a, double[] b) {
        return new double[]{
                a[1] * b[2] - a[2] * b[1],
                a[2] * b[0] - a[0] * b[2],
                a[0] * b[1] - a[1] * b[0]};
    }

    private static double[][] rotX(double t) {
        return new double[][]{{1, 0, 0}, {0, Math.cos(t), -Math.sin(t)}, {0, Math.sin(t), Math.cos(t)}};
    }

    private static double[][] rotY(double t) {
        return new double[][]{{Math.cos(t), 0, Math.sin(t)}, {0, 1, 0}, {-Math.sin(t), 0, Math.cos(t)}};
    }

    private static double[][] rotZ(double t) {
        return new double[][]{{Math.cos(t), -Math.sin(t), 0}, {Math.sin(t), Math.cos(t), 0}, {0, 0, 1}};
    }

    private static double[][] mul(double[][] a, double[][] b) {
        double[][] r = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) r[i][j] += a[i][k] * b[k][j];
            }
        }
        return r;
    }

    private static double[][] transpose(double[][] a) {
        double[][] r = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) r[i][j] = a[j][i];
        }
        return r;
    }

    private static double[] apply(double[][] m, double[] v) {
        double[] r = new double[3];
        for (int i = 0; i < 3; i++) r[i] = m[i][0] * v[0] + m[i][1] * v[1] + m[i][2] * v[2];
        return r;
    }
}
