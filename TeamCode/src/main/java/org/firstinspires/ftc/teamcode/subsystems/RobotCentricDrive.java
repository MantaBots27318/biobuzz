package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.command.SubsystemBase;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;

import org.firstinspires.ftc.teamcode.robot.HardwareNames;

/**
 * Base roulante mecanum sans Pedro ni odométrie, pilotée par rapport au robot (avant = avant du robot).
 * Temporaire : sert à rouler tant que le Pinpoint n'est pas monté et Pedro pas réglé.
 * Ensuite, c'est Drivetrain (Pedro) qui prend le relais.
 */
@Configurable
public class RobotCentricDrive extends SubsystemBase {
    /** Puissance max en mode lent (0 à 1). */
    public static double SLOW_MODE_SCALE = 0.35;

    // Sens des moteurs, lus à la création : vérifier roue par roue avec System Check
    public static boolean FRONT_LEFT_REVERSED = true;
    public static boolean BACK_LEFT_REVERSED = true;
    public static boolean FRONT_RIGHT_REVERSED = false;
    public static boolean BACK_RIGHT_REVERSED = false;

    private final MotorEx frontLeft;
    private final MotorEx backLeft;
    private final MotorEx frontRight;
    private final MotorEx backRight;

    public RobotCentricDrive(HardwareMap hardwareMap) {
        frontLeft = createMotor(hardwareMap, HardwareNames.FRONT_LEFT, FRONT_LEFT_REVERSED);
        backLeft = createMotor(hardwareMap, HardwareNames.BACK_LEFT, BACK_LEFT_REVERSED);
        frontRight = createMotor(hardwareMap, HardwareNames.FRONT_RIGHT, FRONT_RIGHT_REVERSED);
        backRight = createMotor(hardwareMap, HardwareNames.BACK_RIGHT, BACK_RIGHT_REVERSED);
    }

    private static MotorEx createMotor(HardwareMap hardwareMap, String name, boolean reversed) {
        MotorEx motor = new MotorEx(hardwareMap, name);
        motor.setInverted(reversed);
        motor.setRunMode(Motor.RunMode.RawPower);
        // Le robot s'arrête net quand on lâche les sticks au lieu de glisser
        motor.setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);
        return motor;
    }

    /**
     * Toutes les valeurs entre -1 et 1. forward > 0 : avance, strafe > 0 : va à droite,
     * turn > 0 : tourne dans le sens horaire.
     */
    public void drive(double forward, double strafe, double turn, boolean slow) {
        double frontLeftPower = forward + strafe + turn;
        double frontRightPower = forward - strafe - turn;
        double backLeftPower = forward - strafe + turn;
        double backRightPower = forward + strafe - turn;

        // On divise par la plus grande puissance si elle dépasse 1, pour garder la direction demandée
        double max = Math.max(1.0, Math.max(
                Math.max(Math.abs(frontLeftPower), Math.abs(frontRightPower)),
                Math.max(Math.abs(backLeftPower), Math.abs(backRightPower))));
        double scale = (slow ? SLOW_MODE_SCALE : 1.0) / max;

        frontLeft.set(frontLeftPower * scale);
        frontRight.set(frontRightPower * scale);
        backLeft.set(backLeftPower * scale);
        backRight.set(backRightPower * scale);
    }

    public void stop() {
        drive(0, 0, 0, false);
    }
}
