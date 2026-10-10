package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.seattlesolvers.solverslib.command.SubsystemBase;

import org.firstinspires.ftc.teamcode.robot.HardwareNames;

/**
 * Barre levée et abaissée par un servo, au-dessus du sol. En position basse, elle ne doit jamais
 * toucher le sol.
 * Le servo n'est commandé qu'au premier appel de raise() ou lower() : à l'init il ne bouge pas.
 */
@Configurable
public class Bar extends SubsystemBase {
    // TODO à mesurer avec System Check (Y puis pad haut/bas) : DOWN_POSITION doit laisser un jeu au-dessus du sol
    public static double UP_POSITION = 0.5;
    public static double DOWN_POSITION = 0.5;

    private final Servo servo;
    private boolean up = true;

    public Bar(HardwareMap hardwareMap) {
        servo = hardwareMap.get(Servo.class, HardwareNames.BAR);
    }

    public void raise() {
        servo.setPosition(UP_POSITION);
        up = true;
    }

    public void lower() {
        servo.setPosition(DOWN_POSITION);
        up = false;
    }

    public void toggle() {
        if (up) lower();
        else raise();
    }

    public boolean isUp() {
        return up;
    }
}
