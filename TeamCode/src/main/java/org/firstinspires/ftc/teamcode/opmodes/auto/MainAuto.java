package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.seattlesolvers.solverslib.command.CommandOpMode;
import com.seattlesolvers.solverslib.command.InstantCommand;
import com.seattlesolvers.solverslib.command.SequentialCommandGroup;
import com.seattlesolvers.solverslib.command.WaitCommand;

import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.util.TelemetryUtil;

/**
 * Auto minimal sans Pedro : avance tout droit un court instant, puis s'arrête.
 * À remplacer quand l'odométrie sera montée.
 */
@Configurable
@Autonomous(name = "A. Auto", group = "Match", preselectTeleOp = "A. TeleOp")
public class MainAuto extends CommandOpMode {
    // TODO valeurs de remplacement, à régler sur le robot
    public static double DRIVE_POWER = 0.4;
    public static long DRIVE_MS = 1000;

    private Robot robot;

    @Override
    public void initialize() {
        telemetry = TelemetryUtil.withPanels(telemetry);
        reset();
        robot = new Robot(hardwareMap);

        schedule(new SequentialCommandGroup(
                new InstantCommand(() -> robot.drive.drive(DRIVE_POWER, 0, 0, false), robot.drive),
                new WaitCommand(DRIVE_MS),
                new InstantCommand(robot.drive::stop, robot.drive)
        ));
    }
}
