package org.firstinspires.ftc.teamcode.util;

import com.bylazar.telemetry.JoinedTelemetry;
import com.bylazar.telemetry.PanelsTelemetry;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public final class TelemetryUtil {
    private TelemetryUtil() {}

    /**
     * Envoie chaque ligne de télémétrie à la fois au Driver Hub et à Panels.
     * À appeler au début de l'init de chaque OpMode : {@code telemetry = TelemetryUtil.withPanels(telemetry);}
     */
    public static Telemetry withPanels(Telemetry driverHub) {
        return new JoinedTelemetry(PanelsTelemetry.INSTANCE.getFtcTelemetry(), driverHub);
    }
}
