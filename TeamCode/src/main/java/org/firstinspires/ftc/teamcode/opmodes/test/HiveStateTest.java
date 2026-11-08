package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.seattlesolvers.solverslib.command.CommandOpMode;

import org.firstinspires.ftc.teamcode.subsystems.Hives;
import org.firstinspires.ftc.teamcode.subsystems.Vision;
import org.firstinspires.ftc.teamcode.util.TelemetryUtil;

/**
 * Shows, for each CELL with tags, whether it is UP, DOWN, MOVING or NOT SEEN, and the numbers behind it.
 * Set the camera's tilt and height in Panels (Hives settings). Nothing moves.
 * Creates only Vision and Hives (not the whole Robot).
 */
@TeleOp(name = "Hive State Test", group = "Test")
public class HiveStateTest extends CommandOpMode {
    private Vision vision;
    private Hives hives;

    @Override
    public void initialize() {
        reset();
        telemetry = TelemetryUtil.withPanels(telemetry);
        vision = new Vision(hardwareMap);
        hives = new Hives(vision);
        // Vision first: Hives reads its latest tags
        register(vision, hives);
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
        for (Hives.Hive hive : Hives.Hive.values()) {
            telemetry.addData(hive + " HIVE", "%s cell is up (last known: %s); %s",
                    hives.getSideUp(hive), hives.getLastKnownSideUp(hive), hives.getRule(hive));
        }
        for (Hives.Cluster cluster : Hives.Cluster.values()) {
            telemetry.addData(cluster.name(), "%s (last known %s) %s",
                    hives.getState(cluster), hives.getLastKnownState(cluster), hives.getInfo(cluster));
        }
        telemetry.update();
    }

    @Override
    public void end() {
        vision.stop();
    }
}
