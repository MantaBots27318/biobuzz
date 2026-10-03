package org.firstinspires.ftc.teamcode.robot;

/**
 * Tous les noms de la configuration du Driver Hub, au même endroit.
 * Ils doivent être identiques (majuscules comprises) à la config active sur le Control Hub.
 * Le câblage (port -> nom) est documenté dans TeamCode/README.md.
 */
public final class HardwareNames {
    private HardwareNames() {}

    // Base roulante (mecanum)
    public static final String FRONT_LEFT = "frontLeft";
    public static final String FRONT_RIGHT = "frontRight";
    public static final String BACK_LEFT = "backLeft";
    public static final String BACK_RIGHT = "backRight";

    // Odométrie
    public static final String PINPOINT = "pinpoint";

    // Mécanismes
    public static final String INTAKE = "intake";
}
