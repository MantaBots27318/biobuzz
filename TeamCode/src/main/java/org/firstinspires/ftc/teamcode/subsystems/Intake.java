package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.command.SubsystemBase;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;

import org.firstinspires.ftc.teamcode.robot.HardwareNames;

/**
 * Exemple de mécanisme : à adapter au robot BIOBUZZ.
 * Modèle à suivre pour chaque subsystem : les réglages en haut, une API de haut niveau
 * (collect / eject / stop), et aucun accès au hardware en dehors de cette classe.
 */
public class Intake extends SubsystemBase {
    public static double COLLECT_POWER = 1.0;
    public static double EJECT_POWER = -0.6;

    // MotorEx n'envoie une nouvelle puissance que si elle a changé, ce qui évite des écritures inutiles sur le hub
    private final MotorEx motor;

    public Intake(HardwareMap hardwareMap) {
        motor = new MotorEx(hardwareMap, HardwareNames.INTAKE);
    }

    public void collect() {
        motor.set(COLLECT_POWER);
    }

    public void eject() {
        motor.set(EJECT_POWER);
    }

    public void stop() {
        motor.set(0);
    }
}
