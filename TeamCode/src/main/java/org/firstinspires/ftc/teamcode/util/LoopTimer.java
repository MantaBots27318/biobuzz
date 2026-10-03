package org.firstinspires.ftc.teamcode.util;

import com.qualcomm.robotcore.util.ElapsedTime;

/** Mesure la fréquence de la boucle. À afficher en télémétrie pour repérer tout ralentissement. */
public class LoopTimer {
    private final ElapsedTime timer = new ElapsedTime();
    private double lastLoopMs;

    /** À appeler une fois par tour de boucle. */
    public void tick() {
        lastLoopMs = timer.milliseconds();
        timer.reset();
    }

    public double hz() {
        return lastLoopMs > 0 ? 1000.0 / lastLoopMs : 0;
    }

    public double ms() {
        return lastLoopMs;
    }
}
