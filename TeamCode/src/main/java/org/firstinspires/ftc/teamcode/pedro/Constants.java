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
 * The robot's Pedro Pathing settings.
 *
 * Values marked TODO come from AutoTune, to run in this order:
 * Mecanum Tuner -> Pinpoint Tuner -> Foresight Tuner -> Tests.
 * Each tuner prints the Java code to paste here.
 */
public class Constants {
    public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftName.set(HardwareNames.FRONT_LEFT);
        c.frontRightName.set(HardwareNames.FRONT_RIGHT);
        c.backLeftName.set(HardwareNames.BACK_LEFT);
        c.backRightName.set(HardwareNames.BACK_RIGHT);

        // TODO Mecanum Tuner: motor directions
        c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.frontRightDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);
    });

    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.name.set(HardwareNames.PINPOINT);
        c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);

        // TODO Pinpoint Tuner: odometry pod offsets and directions (inches)
        c.xPodOffset.set(0.0);
        c.yPodOffset.set(0.0);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
    });

    // TODO Foresight Tuner: paste the generated values here. While this is empty,
    // createFollower() refuses to start with a clear message.
    public static ForesightConfig foresightConfig = new ForesightConfig(c -> {
    });

    public static Follower createFollower(HardwareMap hardwareMap) {
        Foresight foresight;
        try {
            foresight = new Foresight(foresightConfig);
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Pedro is not tuned yet: run the Foresight Tuner "
                    + "and paste its values into pedro/Constants.java", e);
        }
        return new Follower(
                new PinpointLocalizer(hardwareMap, localizerConfig),
                new Mecanum(hardwareMap, drivetrainConfig),
                foresight);
    }
}
