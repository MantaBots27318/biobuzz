package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.seattlesolvers.solverslib.command.SubsystemBase;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.AimLogic;

import java.util.ArrayList;
import java.util.List;

/**
 * Works out how much the robot must turn to face the up CELL of our HIVE, from the Limelight only (it is locked
 * in place on the robot, so a tag's left-right angle is the aiming error). It does not move the robot: the
 * OpMode passes turnPower() to the drivetrain. Needs Vision and Hives to be updated first.
 *
 * The turn direction and the gains are not yet verified on the robot: tune at HQ.
 */
@Configurable
public class HiveAim extends SubsystemBase {
    // Turn power per degree of error, and its limits
    public static double TURN_KP = 0.02;
    public static double MAX_TURN_POWER = 0.4;
    // Enough to overcome friction, used while the error is outside the tolerance
    public static double MIN_TURN_POWER = 0.1;
    public static double TOLERANCE_DEG = 1.5;
    // If the launcher does not point exactly where the camera does
    public static double AIM_OFFSET_DEG = 0;
    public static long MAX_READING_AGE_MS = 250;

    private final Vision vision;
    private final Hives hives;
    private Alliance alliance = Alliance.BLUE;

    private boolean hasTarget;
    private double errorDeg;
    private double turn;
    private String targetName = "none";
    private int tagCount;

    public HiveAim(Vision vision, Hives hives) {
        this.vision = vision;
        this.hives = hives;
    }

    /** Which HIVE is ours: the alliance chosen at INIT. */
    public void setAlliance(Alliance alliance) {
        this.alliance = alliance;
    }

    @Override
    public void periodic() {
        Hives.Hive hive = alliance == Alliance.BLUE ? Hives.Hive.BLUE : Hives.Hive.RED;
        Hives.Cluster far = hive == Hives.Hive.BLUE ? Hives.Cluster.BLUE_FAR : Hives.Cluster.RED_FAR;
        Hives.Cluster audience = hive == Hives.Hive.BLUE ? Hives.Cluster.BLUE_AUDIENCE : Hives.Cluster.RED_AUDIENCE;

        // The up cell; if unknown, the last known up cell; if unknown too, any tag of our HIVE
        Hives.Side side = hives.getSideUp(hive);
        if (side == Hives.Side.UNKNOWN) side = hives.getLastKnownSideUp(hive);

        List<double[]> tags = new ArrayList<>();
        boolean fresh = vision.getAgeMs() >= 0 && vision.getAgeMs() <= MAX_READING_AGE_MS;
        if (fresh) {
            for (Vision.Tag tag : vision.getTags()) {
                boolean wanted = side == Hives.Side.FAR ? far.contains(tag.id)
                        : side == Hives.Side.AUDIENCE ? audience.contains(tag.id)
                        : far.contains(tag.id) || audience.contains(tag.id);
                if (wanted) tags.add(new double[]{tag.xIn, tag.yIn, tag.zIn});
            }
        }

        tagCount = tags.size();
        hasTarget = !tags.isEmpty();
        if (!hasTarget) {
            errorDeg = 0;
            turn = 0;
            targetName = "none";
            return;
        }
        targetName = side == Hives.Side.FAR ? far.name() : side == Hives.Side.AUDIENCE ? audience.name()
                : hive + " HIVE (any cell)";
        errorDeg = AimLogic.medianBearingDeg(tags, Hives.CAMERA_TILT_DEG) - AIM_OFFSET_DEG;
        turn = AimLogic.turnPower(errorDeg, TURN_KP, MAX_TURN_POWER, MIN_TURN_POWER, TOLERANCE_DEG);
    }

    /** True when a tag of the target is in view, so the aim can be used. */
    public boolean hasTarget() {
        return hasTarget;
    }

    /** The turn to give the drivetrain (counter-clockwise positive); 0 with no target or when aimed. */
    public double turnPower() {
        return turn;
    }

    public boolean isAimed() {
        return hasTarget && Math.abs(errorDeg) <= TOLERANCE_DEG;
    }

    /** Positive when the target is to the right of where the camera points. */
    public double getErrorDeg() {
        return errorDeg;
    }

    public String getTargetName() {
        return targetName;
    }

    public int getTagCount() {
        return tagCount;
    }
}
