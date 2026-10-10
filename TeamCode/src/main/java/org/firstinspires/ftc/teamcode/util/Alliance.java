package org.firstinspires.ftc.teamcode.util;

import com.pedropathing.api.PoseFactory;

/**
 * Lets us write a single Auto: every pose is written for the blue side (Pedro coordinates,
 * 144 x 144 inch field, angles in degrees) and poses() transforms them for red.
 */
public enum Alliance {
    BLUE,
    RED;

    public PoseFactory poses() {
        PoseFactory blue = PoseFactory.degrees();
        // The BIOBUZZ field has 180° rotational symmetry around its center (manual, section 9:
        // GARDENS in opposite corners, red and blue cells on either side of the HIVE),
        // so (x, y, θ) -> (144 - x, 144 - y, θ + 180°)
        return this == BLUE ? blue : blue.mirrorAroundPoint(72, 72);
    }
}
