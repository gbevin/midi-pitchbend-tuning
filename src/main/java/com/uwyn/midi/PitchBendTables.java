/*
 * Copyright 2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package com.uwyn.midi;

import rife.tools.FileUtils;

import java.io.File;

import static java.lang.Math.max;
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
        var out = new StringBuilder("Offset in HCU,PB MIDI1,PB Hex MIDI2,𝚫PB MIDI1,Pitch from MIDI1,PB MIDI2,PB Hex MIDI2,𝚫PB MIDI2,Pitch from MIDI2\n");
        long last_pb1 = 0;
        long last_pb2 = 0;
        for (long offset_cu = -rangeCU; offset_cu <= rangeCU; ++offset_cu) {
            long pb1 = calcPitchBendMIDI1(offset_cu, rangeCU);
            long p1 = calcPitchMIDI1(pb1, rangeCU);
            long d1 = pb1 - last_pb1;
            long pb2 = calcPitchBendMIDI2(offset_cu, rangeCU);
            long p2 = calcPitchMIDI2(pb2, rangeCU);
            long d2 = pb2 - last_pb2;
            out.append(offset_cu);
            out.append(SEPARATOR);
            out.append(pb1);
            out.append(SEPARATOR);
            out.append("\"0x");
            out.append(String.format("%1$4s", Long.toHexString(pb1).toUpperCase()).replace(' ', '0'));
            out.append("\"");
            out.append(SEPARATOR);
            out.append(d1);
            out.append(SEPARATOR);
            out.append(p1);
            out.append(SEPARATOR);
            out.append(pb2);
            out.append(SEPARATOR);
            out.append("\"0x");
            out.append(String.format("%1$8s", Long.toHexString(pb2).toUpperCase()).replace(' ', '0'));
            out.append("\"");
            out.append(SEPARATOR);
            out.append(d2);
            out.append(SEPARATOR);
            out.append(p2);
            out.append("\n");
            last_pb1 = pb1;
            last_pb2 = pb2;
        }
        return out.toString();
    }

    /**
     * This calculates pitch bend according to the formula:
     * {@code min((pitchOffset * 0x2000 / pitchSense) + 0x2000, 0x3FFF))
     * <p>
     * In order to not have to use floating point math, pitch offset and range
     * are expressed in cents.
     * <p>
     * @param pitchOffsetCU the pitch offset in cents
     * @param pitchSenseCU the pitch sensitivity in cents
     * @return the pitch bend value
     */
    public static long calcPitchBendMIDI1(long pitchOffsetCU, long pitchSenseCU) {
        return min((pitchOffsetCU * 0x2000L / pitchSenseCU) + 0x2000L, 0x3FFFL);
    }

    /**
     * This calculates pitch according to the formula:
     * {@code pitchSense * max(((pitchBendValue - 0x2000) / 0x1FFF),-1))}
     * <p>
     * However, in order to not use floating point match, the formula has been
     * rearranged to:
     * {@code max((pitchSense * (pitchBendValue - 0x2000) / 0x1FFF), -pitchSense)}
     * <p>
     * In order to not have to use floating point math, pitch range
     * is expressed in cents.
     */
    public static long calcPitchMIDI1(long pitchBendValue, long pitchSenseCU) {
        return max((pitchSenseCU * (pitchBendValue - 0x2000L) / 0x1FFFL), -pitchSenseCU);
    }

    /**
     * This calculates pitch bend according to the formula:
     * {@code min((pitchOffset * 0x80000000 / pitchSense) + 0x80000000, 0xFFFFFFFF))
     * <p>
     * In order to not have to use floating point math, pitch offset and range
     * are expressed in cents.
     * <p>
     * @param pitchOffsetCU the pitch offset in cents
     * @param pitchSenseCU the pitch sensitivity in cents
     * @return the pitch bend value
     */
    public static long calcPitchBendMIDI2(long pitchOffsetCU, long pitchSenseCU) {
        return min((pitchOffsetCU * 0x80000000L / pitchSenseCU) + 0x80000000L, 0xFFFFFFFFL);
    }

    /**
     * This calculates pitch according to the formula:
     * {@code pitchSense * max(((pitchBendValue - 0x80000000) / 0x7FFFFFFF),-1))}
     * <p>
     * However, in order to not use floating point match, the formula has been
     * rearranged to:
     * {@code max((pitchSense * (pitchBendValue - 0x80000000) / 0x7FFFFFFF), -pitchSense)}
     * <p>
     * In order to not have to use floating point math, pitch range
     * is expressed in cents.
     */
    public static long calcPitchMIDI2(long pitchBendValue, long pitchSenseCU) {
        return max((pitchSenseCU * (pitchBendValue - 0x80000000L) / 0x7FFFFFFFL), -pitchSenseCU);
    }
}