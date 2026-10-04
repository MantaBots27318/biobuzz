package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Sloth Test", group = "Test")
public class SlothTest extends OpMode {
    @Override public void init() {}

    @Override public void loop() {
        telemetry.addLine("Version 1");
        telemetry.update();
    }
}