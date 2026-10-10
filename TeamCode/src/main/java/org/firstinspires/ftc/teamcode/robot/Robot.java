package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.command.CommandScheduler;

import org.firstinspires.ftc.teamcode.subsystems.Bar;
import org.firstinspires.ftc.teamcode.subsystems.RobotCentricDrive;

/**
 * Le robot complet : 4 moteurs de base roulante et le servo de la barre, sans Pedro ni odométrie.
 * Partagé par le TeleOp et l'Auto : un OpMode ne touche jamais au hardwareMap directement.
 * À créer dans initialize(), après reset() du CommandScheduler.
 */
public class Robot {
    public final BatteryVoltage battery;
    public final RobotCentricDrive drive;
    public final Bar bar;

    public Robot(HardwareMap hardwareMap) {
        // Bulk reads : une seule lecture groupée par hub et par boucle, le cache est vidé
        // par le CommandScheduler à la fin de chaque run()
        CommandScheduler.getInstance().setBulkReading(hardwareMap, LynxModule.BulkCachingMode.MANUAL);

        battery = new BatteryVoltage(hardwareMap);
        drive = new RobotCentricDrive(hardwareMap);
        bar = new Bar(hardwareMap);

        CommandScheduler.getInstance().registerSubsystem(drive, bar);
    }
}
