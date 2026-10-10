package org.firstinspires.ftc.teamcode.robot;

/**
 * Every Driver Hub configuration name, in one place.
 * They must match the active configuration on the Control Hub exactly (case included).
 * The wiring (port -> name) is documented in TeamCode/README.md.
 */
public final class HardwareNames {
    private HardwareNames() {}

    // Drivetrain (mecanum)
    public static final String FRONT_LEFT = "frontLeft";
    public static final String FRONT_RIGHT = "frontRight";
    public static final String BACK_LEFT = "backLeft";
    public static final String BACK_RIGHT = "backRight";

    // Odometry
    public static final String PINPOINT = "pinpoint";

    // Mechanisms
    public static final String INTAKE = "intake";
}
