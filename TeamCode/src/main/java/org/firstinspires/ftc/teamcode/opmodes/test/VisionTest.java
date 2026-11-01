package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.seattlesolvers.solverslib.command.CommandOpMode;

import org.firstinspires.ftc.teamcode.subsystems.Vision;
import org.firstinspires.ftc.teamcode.util.TelemetryUtil;

/**
 * Shows every AprilTag the Limelight sees. Nothing moves.
 * Creates only Vision (not the whole Robot) so it runs on any robot that has a "limelight" in its
 * configuration, whatever its other devices.
 */
@TeleOp(name = "Vision Test", group = "Test")
public class VisionTest extends CommandOpMode {
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
        telemetry.addData("Limelight connected", vision.isConnected());
        if (vision.getTags().isEmpty()) {
            telemetry.addLine("No tags in view");
        } else {
            telemetry.addData("Tags in view", vision.getTags().size());
            for (Vision.Tag tag : vision.getTags()) {
                telemetry.addData("Tag " + tag.id, "%.1f in away, %.1f deg %s",
                        tag.distanceIn, Math.abs(tag.bearingDeg), tag.bearingDeg >= 0 ? "right" : "left");
            }
        }
        telemetry.addData("Reading age", "%d ms", vision.getAgeMs());
        telemetry.update();
    }

    @Override
    public void end() {
        vision.stop();
    }
}
