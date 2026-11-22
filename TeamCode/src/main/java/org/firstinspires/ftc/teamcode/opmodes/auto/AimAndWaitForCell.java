package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.seattlesolvers.solverslib.command.CommandBase;

import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.subsystems.HiveAim;
import org.firstinspires.ftc.teamcode.subsystems.Hives;
import org.firstinspires.ftc.teamcode.vision.AimLogic;

/**
 * Waits for the target CELL to be up and still, facing our HIVE the whole time: by the Pinpoint's position and
 * heading while the cell is not up (or its tags are not in view), by the Limelight once it is.
 * It finishes when the cell is up and aimed for AIM_HOLD_MS, or at the time limit. On the time limit it
 * still counts as a success if the cell is up (the path already points us roughly at the HIVE); if the
 * cell was never seen up it is a failure, so the Auto skips the shot.
 */
@Configurable
public class AimAndWaitForCell extends CommandBase {
    /** The cell must stay up and aimed this long before we call it good. */
    public static long AIM_HOLD_MS = 200;
    /**
     * How well aimed counts as ready. Looser than HiveAim.TOLERANCE_DEG: in the first test the error sat at 1.5 deg,
     * right on that limit, and the Auto waited about 3 s too long.
     */
    public static double READY_TOLERANCE_DEG = 3;
    /**
     * The camera turn used by this Auto (not HiveAim's, which the TeleOp uses). In the second test the error sat at
     * 6 deg with 0.12 turn power and the robot did not move: it needs at least about 0.18 to overcome friction.
     */
    public static double AIM_KP = 0.03;
    public static double AIM_MAX_POWER = 0.4;
    public static double AIM_MIN_POWER = 0.18;
    public static double AIM_STOP_DEG = 2;
    /** Facing the HIVE from the Pinpoint: turn power per degree of error, its limit and its dead zone. */
    public static double HOLD_KP = 0.015;
    public static double HOLD_MAX_POWER = 0.3;
    public static double HOLD_TOLERANCE_DEG = 2;

    private final Drivetrain drivetrain;
    private final Hives hives;
    private final HiveAim aim;
    private final Hives.Cluster target;
    private final double hiveX;
    private final double hiveY;
    private final long timeoutMs;

    private final ElapsedTime total = new ElapsedTime();
    private final ElapsedTime ready = new ElapsedTime();
    private boolean wasReady;
    private boolean succeeded;

    public AimAndWaitForCell(Drivetrain drivetrain, Hives hives, HiveAim aim, Hives.Cluster target,
                             double hiveX, double hiveY, long timeoutMs) {
        this.drivetrain = drivetrain;
        this.hives = hives;
        this.aim = aim;
        this.target = target;
        this.hiveX = hiveX;
        this.hiveY = hiveY;
        this.timeoutMs = timeoutMs;
        addRequirements(drivetrain);
    }

    @Override
    public void initialize() {
        total.reset();
        ready.reset();
        wasReady = false;
        succeeded = false;
    }

    @Override
    public void execute() {
        // The camera only steers once the target cell is up: before that it would chase tags that flicker in view
        boolean cellUp = hives.isCellUp(target);
        double turn = cellUp && aim.hasTarget()
                ? AimLogic.turnPower(aim.getErrorDeg(), AIM_KP, AIM_MAX_POWER, AIM_MIN_POWER, AIM_STOP_DEG)
                : turnFromPinpoint();
        drivetrain.driveFieldCentric(0, 0, turn);

        boolean readyNow = cellUp && aim.hasTarget() && Math.abs(aim.getErrorDeg()) <= READY_TOLERANCE_DEG;
        if (readyNow && !wasReady) ready.reset();
        wasReady = readyNow;
    }

    /** Turn toward the HIVE from where the Pinpoint says we are (counter-clockwise positive). */
    private double turnFromPinpoint() {
        Pose pose = drivetrain.pose();
        double wanted = Math.atan2(hiveY - pose.y(), hiveX - pose.x());
        double error = Math.toDegrees(wanted - pose.heading());
        error = ((error + 180) % 360 + 360) % 360 - 180;
        if (Math.abs(error) <= HOLD_TOLERANCE_DEG) return 0;
        return Math.max(-HOLD_MAX_POWER, Math.min(HOLD_MAX_POWER, HOLD_KP * error));
    }

    @Override
    public boolean isFinished() {
        boolean held = wasReady && ready.milliseconds() >= AIM_HOLD_MS;
        return held || total.milliseconds() >= timeoutMs;
    }

    @Override
    public void end(boolean interrupted) {
        succeeded = !interrupted && (wasReady || hives.isCellUp(target));
        // Hold this pose (as after a path) instead of leaving the motors at zero: the robot must not drift while it shoots
        drivetrain.follower.hold(drivetrain.pose());
    }

    /** True when the target cell was seen up, so it is safe to shoot. Read it after the command ended. */
    public boolean succeeded() {
        return succeeded;
    }
}
