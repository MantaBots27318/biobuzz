package org.firstinspires.ftc.teamcode.util;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Writes one CSV file per run on the Control Hub, in /sdcard/FIRST/biobuzz/, to analyse a run afterwards
 * (pull it with: adb pull /sdcard/FIRST/biobuzz). A logging problem never stops the OpMode: it just turns the
 * log off and says why in getStatus().
 */
public class CsvLog {
    private BufferedWriter writer;
    private String status = "off";
    private long lastFlushMs;

    /** Starts a new file named name-<time>.csv with the given column names. */
    public void open(String name, String header) {
        try {
            File dir = new File(AppUtil.FIRST_FOLDER, "biobuzz");
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
            File file = new File(dir, name + "-" + System.currentTimeMillis() + ".csv");
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(header);
            writer.newLine();
            status = "writing " + file.getName();
        } catch (IOException | RuntimeException e) {
            writer = null;
            status = "off (" + e.getMessage() + ")";
        }
    }

    /** One row. Commas and line breaks in text are replaced, so the columns stay aligned. */
    public void row(Object... values) {
        if (writer == null) return;
        try {
            StringBuilder line = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                if (i > 0) line.append(',');
                line.append(String.valueOf(values[i]).replace(',', ';').replace('\n', ' '));
            }
            writer.write(line.toString());
            writer.newLine();
            long now = System.currentTimeMillis();
            if (now - lastFlushMs > 500) {
                writer.flush();
                lastFlushMs = now;
            }
        } catch (IOException e) {
            writer = null;
            status = "off (" + e.getMessage() + ")";
        }
    }

    public void close() {
        if (writer == null) return;
        try {
            writer.close();
        } catch (IOException ignored) {
            // nothing useful to do
        }
        writer = null;
        status = "closed";
    }

    public String getStatus() {
        return status;
    }
}
