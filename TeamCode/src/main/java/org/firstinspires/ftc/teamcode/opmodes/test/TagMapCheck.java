package org.firstinspires.ftc.teamcode.opmodes.test;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.seattlesolvers.solverslib.command.CommandOpMode;

import org.firstinspires.ftc.teamcode.subsystems.Vision;
import org.firstinspires.ftc.teamcode.util.TelemetryUtil;
import org.firstinspires.ftc.teamcode.vision.TagMap;

/**
 * Checks the tag map: from each tag the camera sees, works out where the camera is on the field and
 * compares it with where you say it really is (measure it with a tape measure, type it in Panels).
 * Creates only Vision (not the whole Robot) so it runs with any configuration that has a limelight.
 */
@Configurable
@TeleOp(name = "Tag Map Check", group = "Test")
public class TagMapCheck extends CommandOpMode {
    // Where the camera really is, from the audience/red corner (inches)
    public static double CAMERA_X_IN = 72;
    public static double CAMERA_Y_IN = 30;
    // Optional: leave at -1 if you did not measure the height
    public static double CAMERA_HEIGHT_IN = -1;
    // The real position of each HIVE (far = opposite the audience)
    public static boolean BLUE_FAR_CELL_UP = true;
    public static boolean RED_FAR_CELL_UP = false;

    private Vision vision;

    @Override
    public void initialize() {
        reset();
        telemetry = TelemetryUtil.withPanels(telemetry);
        vision = new Vision(hardwareMap);
        register(vision);
        telemetry.addLine("Press START to turn the Limelight on");
        telemetry.update();
    }

    @Override
    public void preRun() {
        vision.start();
    }

    @Override
    public void run() {
        super.run();
        if (vision.getTags().isEmpty()) {
            telemetry.addLine("No tags in view");
        }
        for (Vision.Tag tag : vision.getTags()) {
            TagMap.TagPose pose = TagMap.pose(tag.id, BLUE_FAR_CELL_UP, RED_FAR_CELL_UP);
            if (pose == null) continue;
            TagMap.CameraPose cam = TagMap.cameraFromTag(pose, tag.xIn, tag.yIn, tag.zIn,
                    tag.rollDeg, tag.pitchDeg, tag.yawDeg);
            double flatFromTag = Math.hypot(cam.x - pose.x, cam.y - pose.y);
            double flatTyped = Math.hypot(CAMERA_X_IN - pose.x, CAMERA_Y_IN - pose.y);
            double error = Math.hypot(cam.x - CAMERA_X_IN, cam.y - CAMERA_Y_IN);
            telemetry.addData("Tag " + tag.id, "camera at x %.1f y %.1f h %.1f in", cam.x, cam.y, cam.z);
            telemetry.addData("  error", "%.1f in off the typed spot; flat distance %.1f (reading) vs %.1f (typed)",
                    error, flatFromTag, flatTyped);
            if (CAMERA_HEIGHT_IN >= 0) {
                telemetry.addData("  height error", "%.1f in", cam.z - CAMERA_HEIGHT_IN);
            }
            telemetry.addData("  camera points", "heading %.0f deg, tilt %.0f deg up", cam.headingDeg, cam.tiltDeg);
        }
        telemetry.update();
    }

    @Override
    public void end() {
        vision.stop();
    }
}
