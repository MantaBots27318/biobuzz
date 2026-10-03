package org.firstinspires.ftc.teamcode.subsystems;

import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.command.SubsystemBase;

import org.firstinspires.ftc.teamcode.pedro.Constants;

/**
 * Base roulante. Le Follower Pedro gère à la fois le pilotage manuel (TeleOp) et le suivi de
 * trajectoires (Auto, via FollowPathCommand). periodic() est appelé une fois par boucle par le
 * CommandScheduler : c'est le seul endroit où on appelle follower.update().
 */
public class Drivetrain extends SubsystemBase {
    public final Follower follower;

    public Drivetrain(HardwareMap hardwareMap) {
        follower = Constants.createFollower(hardwareMap);
    }

    @Override
    public void periodic() {
        follower.update();
    }

    /**
     * Pilotage relatif au terrain. Repère Pedro : +x vers l'avant, +y vers la gauche,
     * rotation positive dans le sens antihoraire.
     */
    public void driveFieldCentric(double forward, double lateral, double turn) {
        follower.manual(ManualDrive.fieldCentric(forward, lateral, turn, follower.pose().heading()));
    }

    public void resetHeading() {
        follower.setHeading(0);
    }

    public void setPose(Pose pose) {
        follower.setPose(pose);
    }

    public Pose pose() {
        return follower.pose();
    }
}
