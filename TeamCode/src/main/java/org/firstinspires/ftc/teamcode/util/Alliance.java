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
        // TODO à vérifier selon la symétrie du terrain BIOBUZZ :
        // miroir gauche/droite = mirrorX(72), miroir haut/bas = mirrorY(72),
        // rotation de 180° autour du centre = mirrorAroundPoint(72, 72)
        return this == BLUE ? blue : blue.mirrorX(72);
    }
}
