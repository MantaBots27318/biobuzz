package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.command.SubsystemBase;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.teamcode.robot.HardwareNames;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The Limelight 3A: reads the AprilTags in view. It only reports where each tag is relative to the
 * camera; turning that into a field position is done elsewhere.
 */
@Configurable
public class Vision extends SubsystemBase {
    public static int PIPELINE = 0;

    /** One tag seen by the camera. Camera frame: x right, y down, z forward (inches). */
    public static class Tag {
        public final int id;
        public final double xIn, yIn, zIn;
        public final double distanceIn;
        public final double bearingDeg;
        // How the tag is turned relative to the camera: rotations about the camera's x, y and z axes
        public final double rollDeg, pitchDeg, yawDeg;

        Tag(int id, double xIn, double yIn, double zIn, double rollDeg, double pitchDeg, double yawDeg) {
            this.id = id;
            this.xIn = xIn;
            this.yIn = yIn;
            this.zIn = zIn;
            this.rollDeg = rollDeg;
            this.pitchDeg = pitchDeg;
            this.yawDeg = yawDeg;
            this.distanceIn = Math.sqrt(xIn * xIn + yIn * yIn + zIn * zIn);
            // Positive = the tag is to the right of the camera's axis
            this.bearingDeg = Math.toDegrees(Math.atan2(xIn, zIn));
        }
    }

    private final Limelight3A limelight;
    private List<Tag> tags = Collections.emptyList();
    private long ageMs = -1;
    private boolean started = false;

    public Vision(HardwareMap hardwareMap) {
        limelight = hardwareMap.get(Limelight3A.class, HardwareNames.LIMELIGHT);
    }

    /** Starts the camera. Call it at START, not in the constructor, so INIT stays light. */
    public void start() {
        limelight.pipelineSwitch(PIPELINE);
        limelight.start();
        started = true;
    }

    public void stop() {
        limelight.stop();
        started = false;
    }

    @Override
    public void periodic() {
        if (!started) return;
        LLResult result = limelight.getLatestResult();
        if (result == null || !result.isValid()) {
            tags = Collections.emptyList();
            ageMs = result == null ? -1 : result.getStaleness();
            return;
        }
        List<Tag> list = new ArrayList<>();
        for (LLResultTypes.FiducialResult f : result.getFiducialResults()) {
            Pose3D p = f.getTargetPoseCameraSpace();
            double toIn = toInches(p.getPosition().unit);
            list.add(new Tag(f.getFiducialId(),
                    p.getPosition().x * toIn,
                    p.getPosition().y * toIn,
                    p.getPosition().z * toIn,
                    p.getOrientation().getRoll(AngleUnit.DEGREES),
                    p.getOrientation().getPitch(AngleUnit.DEGREES),
                    p.getOrientation().getYaw(AngleUnit.DEGREES)));
        }
        tags = list;
        ageMs = result.getStaleness();
    }

    private static double toInches(DistanceUnit unit) {
        return unit.toInches(1);
    }

    /** Tags seen in the latest reading (empty when none, or when the camera is not started). */
    public List<Tag> getTags() {
        return tags;
    }

    /** Age of the latest reading in milliseconds, -1 if there is none. */
    public long getAgeMs() {
        return ageMs;
    }

    public boolean isConnected() {
        return limelight.isConnected();
    }
}
