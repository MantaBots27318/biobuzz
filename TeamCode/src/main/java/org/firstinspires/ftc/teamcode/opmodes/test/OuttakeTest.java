package org.firstinspires.ftc.teamcode.opmodes.test;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.seattlesolvers.solverslib.command.CommandOpMode;
import com.seattlesolvers.solverslib.command.InstantCommand;
import com.seattlesolvers.solverslib.gamepad.GamepadEx;
import com.seattlesolvers.solverslib.gamepad.GamepadKeys;

import org.firstinspires.ftc.teamcode.subsystems.Outtake;
import org.firstinspires.ftc.teamcode.util.LoopTimer;
import org.firstinspires.ftc.teamcode.util.TelemetryUtil;

/**
 * Outtake bench test. Exception to the "go through Robot" rule: Robot creates the Pedro Follower,
 * which does not start until Pedro is tuned.
 *
 * Start / stop: SPIN in Panels (OuttakeTest class), or A on gamepad 1 when a Driver Hub is connected.
 * The speed and gains are changed live in Panels (Outtake class).
 *
 * Tuning, with the RPM graphed in Panels:
 * 1. KP = KI = KD = 0, KV = 1: start it and note how far the measured RPM is from the target.
 * 2. Adjust KV until the measured RPM matches the target at a few speeds.
 * 3. Raise KP until the speed recovers quickly after a ball, without oscillating.
 * 4. Copy the final values into Outtake.java: Panels values are lost on the next deploy.
 */
@Configurable
@TeleOp(name = "Outtake Test", group = "Test")
public class OuttakeTest extends CommandOpMode {
    public static boolean SPIN = false;

    private Outtake outtake;
    private final LoopTimer loopTimer = new LoopTimer();

    @Override
    public void initialize() {
        telemetry = TelemetryUtil.withPanels(telemetry);
        reset();
        SPIN = false;
        outtake = new Outtake(hardwareMap);
        register(outtake);

        GamepadEx driver = new GamepadEx(gamepad1);
        driver.getGamepadButton(GamepadKeys.Button.A).whenPressed(new InstantCommand(() -> SPIN = !SPIN));
    }

    @Override
    public void run() {
        // The A button flips SPIN, so Panels and the gamepad always agree on the state
        if (SPIN != outtake.isOn()) {
            if (SPIN) outtake.spinUp();
            else outtake.stop();
        }
        super.run();

        loopTimer.tick();
        telemetry.addData("Outtake", outtake.isOn() ? "on" : "off");
        telemetry.addData("Target RPM", outtake.targetRpm());
        telemetry.addData("Left RPM", outtake.leftRpm());
        telemetry.addData("Right RPM", outtake.rightRpm());
        telemetry.addData("At speed", outtake.atSpeed() ? "yes" : "no");
        telemetry.addData("Loop", "%.0f Hz", loopTimer.hz());
        telemetry.addLine("SPIN in Panels or A: start / stop");
        telemetry.update();
    }
}
