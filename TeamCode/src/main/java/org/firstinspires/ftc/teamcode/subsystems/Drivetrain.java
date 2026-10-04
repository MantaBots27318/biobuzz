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
 * Base roulante. Le Follower Pedro gère à la fois le pilotage manuel (TeleOp) et le suivi de
 * trajectoires (Auto, via FollowPathCommand). periodic() est appelé une fois par boucle par le
 * CommandScheduler : c'est le seul endroit où on appelle follower.update().
 */
@Configurable
public class Drivetrain extends SubsystemBase {
    /**
     * TODO à mesurer sur le terrain : cap Pedro (en degrés) d'un robot qui s'éloigne du pilote rouge.
     * Le pilote bleu est en face (terrain symétrique par rotation de 180°), d'où + 180° pour lui.
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

    /** Choisit de quel côté du terrain se trouve le pilote, pour le pilotage relatif au terrain. */
    public void setAlliance(Alliance alliance) {
        this.alliance = alliance;
    }

    /**
     * Pilotage relatif au terrain : pousser le stick vers l'avant éloigne toujours le robot du pilote,
     * quelle que soit son orientation. Repère Pedro : +x vers l'avant, +y vers la gauche,
     * rotation positive dans le sens antihoraire.
     */
    public void driveFieldCentric(double forward, double lateral, double turn) {
        follower.manual(ManualDrive.fieldCentric(
                forward, lateral, turn, follower.pose().heading(), -driverForward()));
    }

    /** À utiliser quand le robot est tourné dos au pilote : recale le cap sans toucher à x et y. */
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
