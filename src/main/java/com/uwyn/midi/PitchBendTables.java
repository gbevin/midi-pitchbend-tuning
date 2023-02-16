/*
 * Copyright 2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package com.uwyn.midi;

import rife.tools.FileUtils;

import java.io.File;

import static java.lang.Math.min;

public class PitchBendTables {
    public final static String FILE_PREFIX = "pitch_bend_table_";
    public final static String FILE_EXTENSION = ".csv";
    public final static String SEPARATOR = ",";

    public static void main(String[] args)
    throws Exception {
        long[] ranges_cu = new long[]{100, 200, 500, 700, 1200, 2400, 4800, 9600};

        for (long range_cu : ranges_cu) {
            var out = pitchBendTableFor(range_cu);
            FileUtils.writeString(out, new File(FILE_PREFIX + range_cu + FILE_EXTENSION));
        }
    }

    private static String pitchBendTableFor(long rangeCU) {
        var out = new StringBuilder("offset cents,pb value,pb value hex,delta pb value\n");
        long last_pb = 0;
        for (long offset_cu = -rangeCU; offset_cu <= rangeCU; ++offset_cu) {
            long pb = calcPitchBend(offset_cu, rangeCU);
            long delta = pb - last_pb;
            out.append(offset_cu);
            out.append(SEPARATOR);
            out.append(pb);
            out.append(SEPARATOR);
            out.append("0x");
            out.append(Long.toHexString(pb).toUpperCase());
            out.append(SEPARATOR);
            out.append("𝚫");
            out.append(delta);
            out.append("\n");
            last_pb = pb;
        }
        return out.toString();
    }

    /**
     * This calculates pitch bend according to the formula:
     * {@code min((pitchOffset * 0x2000 / pitchSense) + 0x2000, 0x3FFF))
     *
     * In order to not have to use floating point math, pitch offset and range
     * are expressed in cents.
     *
     * @param pitchOffsetCU the pitch offset in cents
     * @param pitchSenseCU the pitch sensitivity in cents
     * @return the pitch bend value
     */
    public static long calcPitchBend(long pitchOffsetCU, long pitchSenseCU) {
        return min((pitchOffsetCU * 0x2000 / pitchSenseCU) + 0x2000, 0x3FFF);
    }
}