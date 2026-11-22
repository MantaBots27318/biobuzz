package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.localization.Localizer;
import com.pedropathing.localization.MotionState;
import com.pedropathing.math.Pose;

/**
 * Wraps the Pinpoint so the tags can correct x and y without writing to it. Writing a corrected pose back to the
 * Pinpoint also wrote a heading (and x, y) read earlier in the loop: while the robot turned or moved, each write
 * erased a bit of the motion, and the far-start park ended 15 to 20 deg turned (2026-11-22). Here the correction is
 * a shift kept in software and added to every reading; the heading always comes straight from the Pinpoint.
 */
public class ShiftedLocalizer implements Localizer {
    private final Localizer inner;
    private double shiftX = 0;
    private double shiftY = 0;

    public ShiftedLocalizer(Localizer inner) {
        this.inner = inner;
    }

    /** Moves the reported position by (dx, dy) inches. Nothing is written to the Pinpoint. */
    public void shift(double dx, double dy) {
        shiftX += dx;
        shiftY += dy;
    }

    /** A full pose (the start pose): written to the Pinpoint, and the shift starts again from zero. */
    @Override
    public void setPose(Pose pose) {
        inner.setPose(pose);
        shiftX = 0;
        shiftY = 0;
    }

    @Override
    public MotionState state() {
        MotionState s = inner.state();
        Pose p = s.pose();
        return s.withPose(new Pose(p.x() + shiftX, p.y() + shiftY, p.heading()));
    }

    @Override
    public void update() {
        inner.update();
    }

    @Override
    public void reset() {
        inner.reset();
        shiftX = 0;
        shiftY = 0;
    }
}
