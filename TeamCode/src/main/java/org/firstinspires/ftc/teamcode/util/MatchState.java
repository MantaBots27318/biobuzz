package org.firstinspires.ftc.teamcode.util;

import com.pedropathing.math.Pose;

/**
 * What the Auto hands over to the TeleOp: the alliance and the robot's last pose.
 * The Auto saves it every loop, so it is up to date even if the Auto is stopped early.
 * The values are lost when the app restarts and on every Sloth reload.
 */
public final class MatchState {
    /** Past this age, the TeleOp is assumed not to follow that Auto (practice, another match). */
    public static final long MAX_AGE_MS = 3 * 60_000;

    private static Alliance alliance;
    private static Pose pose;
    private static long savedAtMs;

    private MatchState() {}

    public static void save(Alliance alliance, Pose pose) {
        MatchState.alliance = alliance;
        MatchState.pose = pose;
        savedAtMs = System.currentTimeMillis();
    }

    public static boolean isFresh() {
        return pose != null && System.currentTimeMillis() - savedAtMs < MAX_AGE_MS;
    }

    public static Alliance alliance() {
        return alliance;
    }

    public static Pose pose() {
        return pose;
    }
}
