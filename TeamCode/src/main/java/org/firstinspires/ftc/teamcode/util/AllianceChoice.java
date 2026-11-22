package org.firstinspires.ftc.teamcode.util;

import com.qualcomm.robotcore.hardware.Gamepad;

/**
 * The alliance picked during INIT (Square = blue, Circle = red). RED until a button is pressed, with a warning on the
 * screen, so a forgotten press is seen (decided 2026-11-22: warn and keep a default, the same in every OpMode).
 */
public class AllianceChoice {
    public static final Alliance DEFAULT = Alliance.RED;

    private Alliance alliance = DEFAULT;
    private boolean chosen = false;

    /** Starts from an alliance that counts as chosen (for example the one handed over by the Auto). */
    public void preset(Alliance alliance) {
        this.alliance = alliance;
        chosen = true;
    }

    /** Call in initialize_loop(). */
    public void update(Gamepad gamepad) {
        if (gamepad.square) {
            alliance = Alliance.BLUE;
            chosen = true;
        }
        if (gamepad.circle) {
            alliance = Alliance.RED;
            chosen = true;
        }
    }

    public Alliance get() {
        return alliance;
    }

    public boolean isChosen() {
        return chosen;
    }

    /** The first telemetry line of INIT. */
    public String line() {
        return chosen ? String.format("Alliance: %s (chosen)   Square = blue, Circle = red", alliance)
                : String.format("ALLIANCE NOT CHOSEN: %s by default (Square = blue, Circle = red)", alliance);
    }
}
