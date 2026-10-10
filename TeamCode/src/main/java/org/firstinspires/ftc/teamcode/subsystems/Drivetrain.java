package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.command.SubsystemBase;

import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.firstinspires.ftc.teamcode.util.Alliance;

/**
 * Drivetrain. The Pedro Follower handles both manual driving (TeleOp) and path following
 * (Auto, through FollowPathCommand). The CommandScheduler calls periodic() once per loop:
 * it is the only place where follower.update() is called.
 */
@Configurable
public class Drivetrain extends SubsystemBase {
    /**
     * TODO measure on the field: Pedro heading (degrees) of a robot moving away from the red driver.
     * The blue driver is on the opposite side (180° rotational symmetry), hence + 180° for them.
     */
    public static double RED_DRIVER_FORWARD_DEG = 0;

    public final Follower follower;
    private Alliance alliance = Alliance.BLUE;

    public Drivetrain(HardwareMap hardwareMap) {
        follower = Constants.createFollower(hardwareMap);
    }

    @Override
    public void periodic() {
        follower.update();
    }

    /** Sets which side of the field the driver stands on, for field-centric driving. */
    public void setAlliance(Alliance alliance) {
        this.alliance = alliance;
    }

    /**
     * Field-centric driving: pushing the stick forward always moves the robot away from the driver,
     * whatever its orientation. Pedro frame: +x forward, +y left, positive rotation is
     * counter-clockwise.
     */
    public void driveFieldCentric(double forward, double lateral, double turn) {
        follower.manual(ManualDrive.fieldCentric(
                forward, lateral, turn, follower.pose().heading(), -driverForward()));
    }

    /** Use when the robot faces away from the driver: resets the heading without changing x and y. */
    public void resetHeading() {
        follower.setHeading(driverForward());
    }

    public void setPose(Pose pose) {
        follower.setPose(pose);
    }

    public Pose pose() {
        return follower.pose();
    }

    private double driverForward() {
        double deg = RED_DRIVER_FORWARD_DEG + (alliance == Alliance.BLUE ? 180 : 0);
        return Math.toRadians(deg);
    }
}
