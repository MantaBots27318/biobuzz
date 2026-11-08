package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.seattlesolvers.solverslib.command.CommandOpMode;

import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.subsystems.HiveAim;
import org.firstinspires.ftc.teamcode.subsystems.Hives;
import org.firstinspires.ftc.teamcode.subsystems.Vision;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.TelemetryUtil;

/**
 * Tries the TeleOp aim on its own: drive with the left stick, turn with the right stick, hold RIGHT_BUMPER to
 * turn by itself toward the up CELL of our HIVE. Needs only the drive motors, the Pinpoint and the limelight
 * in the configuration (not the whole Robot). Start with the wheels off the ground or lots of room.
 * X / B during INIT chooses the alliance.
 */
@TeleOp(name = "Aim Test", group = "Test")
public class AimTest extends CommandOpMode {
    private Drivetrain drivetrain;
    private Vision vision;
    private Hives hives;
    private HiveAim aim;
    private Alliance alliance = Alliance.BLUE;

    @Override
    public void initialize() {
        reset();
        telemetry = TelemetryUtil.withPanels(telemetry);
        drivetrain = new Drivetrain(hardwareMap);
        vision = new Vision(hardwareMap);
        hives = new Hives(vision);
        aim = new HiveAim(vision, hives);
        // Vision first, then Hives, then HiveAim: each reads the one before it
        register(drivetrain, vision, hives, aim);
    }

    @Override
    public void initialize_loop() {
        if (gamepad1.x) alliance = Alliance.BLUE;
        if (gamepad1.b) alliance = Alliance.RED;
        telemetry.addData("Alliance", "%s   (X = blue, B = red)", alliance);
        telemetry.addLine("Place the robot facing away from the driver, then press START");
        telemetry.update();
    }

    @Override
    public void preRun() {
        drivetrain.setAlliance(alliance);
        drivetrain.resetHeading();
        aim.setAlliance(alliance);
        vision.start();
    }

    @Override
    public void run() {
        boolean aiming = gamepad1.right_bumper && aim.hasTarget();
        double turn = aiming ? aim.turnPower() : -gamepad1.right_stick_x;
        drivetrain.driveFieldCentric(-gamepad1.left_stick_y, -gamepad1.left_stick_x, turn);
        super.run();

        telemetry.addData("Bumper", gamepad1.right_bumper ? (aiming ? "AIMING" : "held, but no target") : "released");
        telemetry.addData("Target", "%s (%d tag(s))", aim.getTargetName(), aim.getTagCount());
        telemetry.addData("Error", "%.1f deg (positive = target to the right)%s", aim.getErrorDeg(),
                aim.isAimed() ? "  AIMED" : "");
        telemetry.addData("Turn power", "%.2f", aiming ? aim.turnPower() : 0);
        telemetry.addData("HIVE", "%s cell is up", hives.getSideUp(
                alliance == Alliance.BLUE ? Hives.Hive.BLUE : Hives.Hive.RED));
        telemetry.update();
    }

    @Override
    public void end() {
        vision.stop();
    }
}
