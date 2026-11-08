package org.firstinspires.ftc.teamcode.opmodes.test;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.seattlesolvers.solverslib.command.CommandOpMode;

import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.subsystems.Hives;
import org.firstinspires.ftc.teamcode.subsystems.TagLocalizer;
import org.firstinspires.ftc.teamcode.subsystems.Vision;
import org.firstinspires.ftc.teamcode.util.TelemetryUtil;

/**
 * Tries the position correction from AprilTags. Set where you put the robot (start x, y, heading) in Panels, drive
 * with the sticks, press A for a fake bump (moves the pose by 10 in. in x and y) and watch it come back, press B to
 * put the pose back to the start values. Needs only the drive motors, the Pinpoint and the limelight in the
 * configuration (not the whole Robot). Switch the correction on and off with TagLocalizer's ENABLED.
 */
@Configurable
@TeleOp(name = "Localization Test", group = "Test")
public class LocalizationTest extends CommandOpMode {
    // Where the robot is at START, from the audience/red corner; heading 0 = facing the blue wall
    public static double START_X_IN = 72;
    public static double START_Y_IN = 24;
    public static double START_HEADING_DEG = 90;
    // False: the first good tag reading sets the position
    public static boolean POSE_KNOWN = true;

    private Drivetrain drivetrain;
    private Vision vision;
    private TagLocalizer localizer;
    private boolean lastA, lastB;

    @Override
    public void initialize() {
        reset();
        telemetry = TelemetryUtil.withPanels(telemetry);
        drivetrain = new Drivetrain(hardwareMap);
        vision = new Vision(hardwareMap);
        Hives hives = new Hives(vision);
        localizer = new TagLocalizer(drivetrain, vision, hives);
        // Vision first, then Hives, then the localizer: each reads the one before it
        register(drivetrain, vision, hives, localizer);
        telemetry.addLine("Set the start pose in Panels, put the robot there, then press START");
        telemetry.update();
    }

    @Override
    public void preRun() {
        drivetrain.setPose(startPose());
        localizer.setPoseKnown(POSE_KNOWN);
        vision.start();
    }

    @Override
    public void run() {
        drivetrain.driveFieldCentric(-gamepad1.left_stick_y, -gamepad1.left_stick_x, -gamepad1.right_stick_x);
        super.run();

        if (gamepad1.a && !lastA) {
            Pose p = drivetrain.pose();
            drivetrain.setPose(new Pose(p.x() + 10, p.y() + 10, p.heading()));
        }
        if (gamepad1.b && !lastB) drivetrain.setPose(startPose());
        lastA = gamepad1.a;
        lastB = gamepad1.b;

        Pose pose = drivetrain.pose();
        telemetry.addData("Pose", "x %.1f  y %.1f  heading %.0f deg", pose.x(), pose.y(), Math.toDegrees(pose.heading()));
        telemetry.addData("Status", localizer.getStatus());
        telemetry.addData("Last tag measurement", "x %.1f  y %.1f  (%d tag(s))",
                localizer.getMeasuredX(), localizer.getMeasuredY(), localizer.getTagsUsed());
        telemetry.addData("Last error", "%.1f in, gain %.2f, bump counter %d",
                localizer.getLastErrorIn(), localizer.getLastGain(), localizer.getSnapCount());
        telemetry.addData("Heading disagreement", "%.1f deg (information only)", localizer.getHeadingDisagreementDeg());
        telemetry.addData("Position known", localizer.isPoseKnown());
        telemetry.addLine("A = fake bump (+10 in. x and y), B = back to the start pose");
        telemetry.update();
    }

    private static Pose startPose() {
        return new Pose(START_X_IN, START_Y_IN, Math.toRadians(START_HEADING_DEG));
    }

    @Override
    public void end() {
        vision.stop();
    }
}
