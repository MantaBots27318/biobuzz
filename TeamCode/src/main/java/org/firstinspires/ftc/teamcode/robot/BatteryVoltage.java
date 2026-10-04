package org.firstinspires.ftc.teamcode.robot;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

/**
 * Tension de la batterie, relue au plus toutes les REFRESH_MS : chaque lecture est une requête
 * au hub qui ralentit la boucle.
 */
@Configurable
public class BatteryVoltage {
    public static double NOMINAL_VOLTS = 12.0;
    public static double REFRESH_MS = 500;

    private final VoltageSensor sensor;
    private final ElapsedTime sinceRead = new ElapsedTime();
    private double volts = NOMINAL_VOLTS;

    public BatteryVoltage(HardwareMap hardwareMap) {
        sensor = hardwareMap.voltageSensor.iterator().next();
        read();
    }

    public double volts() {
        if (sinceRead.milliseconds() > REFRESH_MS) read();
        return volts;
    }

    /** Puissance à envoyer pour obtenir le même effet qu'avec une batterie à NOMINAL_VOLTS. */
    public double compensate(double power) {
        return Range.clip(power * NOMINAL_VOLTS / volts(), -1, 1);
    }

    private void read() {
        double v = sensor.getVoltage();
        // Une lecture ratée renvoie 0 : on garde la dernière valeur valable
        if (v > 0) volts = v;
        sinceRead.reset();
    }
}
