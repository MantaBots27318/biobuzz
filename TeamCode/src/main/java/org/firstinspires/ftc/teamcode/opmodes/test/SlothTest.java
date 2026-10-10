package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.util.TelemetryUtil;

@TeleOp(name = "Sloth Test", group = "Test")
public class SlothTest extends OpMode {
    @Override public void init() {
        telemetry = TelemetryUtil.withPanels(telemetry);
    }

    @Override public void loop() {
        telemetry.addLine("Version 3");
        telemetry.update();
    }
}