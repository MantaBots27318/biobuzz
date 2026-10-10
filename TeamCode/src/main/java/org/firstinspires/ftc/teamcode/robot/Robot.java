package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.command.CommandScheduler;

import org.firstinspires.ftc.teamcode.subsystems.Bar;
import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.subsystems.Intake;

/**
 * Le robot complet : crée tous les subsystems une seule fois, partagé par le TeleOp et l'Auto.
 * Un OpMode ne touche jamais au hardwareMap directement, il passe par cette classe.
 * À créer dans initialize(), après reset() du CommandScheduler.
 */
public class Robot {
    public final BatteryVoltage battery;
    public final Drivetrain drivetrain;
    public final Intake intake;
    public final Bar bar;

    public Robot(HardwareMap hardwareMap) {
        // Bulk reads : une seule lecture groupée par hub et par boucle, le cache est vidé
        // par le CommandScheduler à la fin de chaque run()
        CommandScheduler.getInstance().setBulkReading(hardwareMap, LynxModule.BulkCachingMode.MANUAL);

        battery = new BatteryVoltage(hardwareMap);
        drivetrain = new Drivetrain(hardwareMap);
        intake = new Intake(hardwareMap, battery);
        bar = new Bar(hardwareMap);

        CommandScheduler.getInstance().registerSubsystem(drivetrain, intake, bar);
    }
}
