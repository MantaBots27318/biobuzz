package org.firstinspires.ftc.teamcode.util;

import com.bylazar.telemetry.JoinedTelemetry;
import com.bylazar.telemetry.PanelsTelemetry;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public final class TelemetryUtil {
    private TelemetryUtil() {}

    /**
     * Sends every telemetry line to both the Driver Hub and Panels.
     * Call it at the start of every OpMode's init: {@code telemetry = TelemetryUtil.withPanels(telemetry);}
     */
    public static Telemetry withPanels(Telemetry driverHub) {
        return new JoinedTelemetry(PanelsTelemetry.INSTANCE.getFtcTelemetry(), driverHub);
    }
}
