package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.follower.Follower;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.robot.HardwareNames;

/**
 * Réglages Pedro Pathing du robot.
 *
 * Les valeurs marquées TODO viennent de l'AutoTune (OpModes « Tuning » sur le Driver Hub),
 * à lancer dans cet ordre : Mecanum Tuner -> Pinpoint Tuner -> Foresight Tuner -> Tests.
 * Chaque tuner affiche le code Java à coller ici.
 */
public class Constants {
    public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftName.set(HardwareNames.FRONT_LEFT);
        c.frontRightName.set(HardwareNames.FRONT_RIGHT);
        c.backLeftName.set(HardwareNames.BACK_LEFT);
        c.backRightName.set(HardwareNames.BACK_RIGHT);

        // TODO Mecanum Tuner : sens des moteurs
        c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.frontRightDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);
    });

    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.name.set(HardwareNames.PINPOINT);
        c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);

        // TODO Pinpoint Tuner : position et sens des roues d'odométrie (en pouces)
        c.xPodOffset.set(0.0);
        c.yPodOffset.set(0.0);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
    });

    // TODO Foresight Tuner : coller ici les valeurs générées. Tant que c'est vide,
    // createFollower() refuse de démarrer avec un message clair.
    public static ForesightConfig foresightConfig = new ForesightConfig(c -> {
    });

    public static Follower createFollower(HardwareMap hardwareMap) {
        Foresight foresight;
        try {
            foresight = new Foresight(foresightConfig);
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Pedro n'est pas encore réglé : lancez le Foresight Tuner "
                    + "et collez ses valeurs dans pedro/Constants.java", e);
        }
        return new Follower(
                new PinpointLocalizer(hardwareMap, localizerConfig),
                new Mecanum(hardwareMap, drivetrainConfig),
                foresight);
    }
}
