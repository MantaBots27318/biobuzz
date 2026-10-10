package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.seattlesolvers.solverslib.command.SubsystemBase;

import org.firstinspires.ftc.teamcode.robot.HardwareNames;

/**
 * Barre d'intake commandée par un servo, avec deux positions : repos et active.
 * Le servo n'est commandé qu'au premier appel de rest() ou activate() : à l'init il ne bouge pas.
 */
@Configurable
public class Bar extends SubsystemBase {
    // TODO valeurs de remplacement, à mesurer avec System Check (Y puis pad haut/bas)
    public static double REST_POSITION = 0;
    public static double ACTIVE_POSITION = 1;

    private final Servo servo;
    private boolean active = false;

    public Bar(HardwareMap hardwareMap) {
        servo = hardwareMap.get(Servo.class, HardwareNames.BAR);
    }

    public void rest() {
        servo.setPosition(REST_POSITION);
        active = false;
    }

    public void activate() {
        servo.setPosition(ACTIVE_POSITION);
        active = true;
    }

    public void toggle() {
        if (active) rest();
        else activate();
    }

    public boolean isActive() {
        return active;
    }
}
