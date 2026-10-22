package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.controllers.Controller;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Vector2D;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.robot.HardwareNames;

/**
 * The robot's Pedro Pathing settings.
 *
 * Values marked TODO come from AutoTune, to run in this order:
 * Mecanum Tuner -> Pinpoint Tuner -> Foresight Tuner -> Tests.
 * Each tuner prints the Java code to paste here.
 */
public class Constants {
    public static MecanumConfig drivetrainConfig = new MecanumConfig(
        c -> {
        c.frontLeftName.set("frontLeft");
        c.frontRightName.set("frontRight");
        c.backLeftName.set("backLeft");
        c.backRightName.set("backRight");
        c.frontLeftDirection.set(DcMotorSimple.Direction.FORWARD);
        c.frontRightDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backLeftDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);
        c.manualBrakeMode.set(true);
    }
    );

    public static PinpointConfig localizerConfig = new PinpointConfig(
        c -> {
        c.name.set(HardwareNames.PINPOINT);
            c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
            c.xPodOffset.set(1.679826871616634);
            c.yPodOffset.set(-6.6598708986297375);
            c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
            c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
            c.globalDistanceUnit.set(DistanceUnit.INCH);
            c.offsetUnits.set(DistanceUnit.INCH);
    }
    );

    // TODO Foresight Tuner: paste the generated values here. While this is empty,
    // createFollower() refuses to start with a clear message.
    public static ForesightConfig foresightConfig = new ForesightConfig(
            c -> {
                Controller primaryTranslationalForward = Controller.proportional(0.5169044181552397);
                Controller secondaryTranslationalForward = Controller.proportional(0.19098237056792983);
                Controller primaryTranslationalLateral = Controller.proportional(1.087793988186525);
                Controller secondaryTranslationalLateral = Controller.proportional(0.40191081224423325);

                c.forwardTranslational.set(Controller.piecewise(secondaryTranslationalForward).put(2.5, primaryTranslationalForward));
                c.strafeTranslational.set(Controller.piecewise(secondaryTranslationalLateral).put(2.5, primaryTranslationalLateral));

                c.coast.set(Controller.proportionalFeedforward(0.014621493601193204));
                c.brake.set(Controller.proportionalFeedforward(0.012428269561014223));

                c.headingFeedback.set(Controller.proportional(8.90471989630108));
                c.headingBrakeCoefficients.set(Vector2D.cartesian(0.06186783688885615, 0.0049578755074748335));

                c.linearBrakeCoefficients.set(Matrix.diag(0.15520802958547872, 0.04328746378182151));
                c.quadraticBrakeCoefficients.set(Matrix.diag(5.253095574323881E-4, 0.0023124715121728085));

                c.maxAchievableForwardVelocity.set(65.08830440213332);
                c.maxAchievableStrafeVelocity.set(48.278370286503716);
                c.naturalForwardDeceleration.set(30.06725111930359);
                c.naturalStrafeDeceleration.set(75.61174988984254);
            }
    );

    public static Follower create(HardwareMap h) {
        return new Follower(
                new PinpointLocalizer(h, localizerConfig),
                new Mecanum(h, drivetrainConfig),
                new Foresight(foresightConfig)
        );
    }
}
