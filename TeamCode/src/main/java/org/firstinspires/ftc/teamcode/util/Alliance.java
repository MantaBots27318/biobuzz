package org.firstinspires.ftc.teamcode.util;

import com.pedropathing.api.PoseFactory;

/**
 * Permet d'écrire un seul Auto : toutes les positions sont écrites côté bleu (coordonnées Pedro,
 * terrain de 144 x 144 pouces, angles en degrés) et poses() les transforme pour le rouge.
 */
public enum Alliance {
    BLUE,
    RED;

    public PoseFactory poses() {
        PoseFactory blue = PoseFactory.degrees();
        // Le terrain BIOBUZZ est symétrique par rotation de 180° autour du centre (manuel, section 9 :
        // GARDENS dans des coins opposés, cellules rouge et bleue de part et d'autre de la HIVE),
        // donc (x, y, θ) -> (144 - x, 144 - y, θ + 180°)
        return this == BLUE ? blue : blue.mirrorAroundPoint(72, 72);
    }
}
