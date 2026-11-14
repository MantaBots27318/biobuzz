package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.command.CommandScheduler;

import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.subsystems.HiveAim;
import org.firstinspires.ftc.teamcode.subsystems.Hives;
import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.TagLocalizer;
import org.firstinspires.ftc.teamcode.subsystems.Vision;

/**
 * The whole robot: creates every subsystem once, shared by the TeleOp and the Auto.
 * An OpMode never touches the hardwareMap directly, it goes through this class.
 * Create it in initialize(), after the CommandScheduler's reset().
 */
public class Robot {
    public final BatteryVoltage battery;
    public final Drivetrain drivetrain;
    public final Intake intake;
    public final Vision vision;
    public final Hives hives;
    public final HiveAim aim;
    public final TagLocalizer localizer;

    public Robot(HardwareMap hardwareMap) {
        // Bulk reads: one grouped read per hub per loop; the CommandScheduler clears the cache
        // at the end of each run()
        CommandScheduler.getInstance().setBulkReading(hardwareMap, LynxModule.BulkCachingMode.MANUAL);

        battery = new BatteryVoltage(hardwareMap);
        drivetrain = new Drivetrain(hardwareMap);
        intake = new Intake(hardwareMap, battery);
        vision = new Vision(hardwareMap);
        hives = new Hives(vision);
        aim = new HiveAim(vision, hives);
        localizer = new TagLocalizer(drivetrain, vision, hives);

        CommandScheduler.getInstance().registerSubsystem(drivetrain, intake, vision, hives, aim, localizer);
    }
}
