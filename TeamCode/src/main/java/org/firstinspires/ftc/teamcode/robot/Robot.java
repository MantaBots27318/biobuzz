package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.command.CommandScheduler;

import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.subsystems.Intake;

/**
 * The whole robot: creates every subsystem once, shared by the TeleOp and the Auto.
 * An OpMode never touches the hardwareMap directly, it goes through this class.
 * Create it in initialize(), after the CommandScheduler's reset().
 */
public class Robot {
    public final BatteryVoltage battery;
    public final Drivetrain drivetrain;
    public final Intake intake;

    public Robot(HardwareMap hardwareMap) {
        // Bulk reads: one grouped read per hub per loop; the CommandScheduler clears the cache
        // at the end of each run()
        CommandScheduler.getInstance().setBulkReading(hardwareMap, LynxModule.BulkCachingMode.MANUAL);

        battery = new BatteryVoltage(hardwareMap);
        drivetrain = new Drivetrain(hardwareMap);
        intake = new Intake(hardwareMap, battery);

        CommandScheduler.getInstance().registerSubsystem(drivetrain, intake);
    }
}
