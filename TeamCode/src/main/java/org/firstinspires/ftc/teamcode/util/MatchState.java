package org.firstinspires.ftc.teamcode.util;

import com.pedropathing.math.Pose;

/**
 * Ce que l'Auto transmet au TeleOp : l'alliance et la dernière position du robot.
 * L'Auto l'enregistre à chaque boucle, pour que ce soit à jour même s'il est arrêté avant la fin.
 * Les valeurs sont perdues au redémarrage de l'app et à chaque rechargement Sloth.
 */
public final class MatchState {
    /** Au-delà, on considère que le TeleOp ne suit pas cet Auto (entraînement, autre match). */
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
