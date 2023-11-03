/*
 * Copyright 2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package com.uwyn.midi;

import rife.tools.FileUtils;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.File;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class PitchBendTables {
    public final static String FILE_PREFIX = "pitch_bend_table_";
    public final static String FILE_EXTENSION = ".csv";
    public final static String SEPARATOR = ",";

    public static void main(String[] args)
    throws Exception {
        var ranges_cu = new long[]{100, 200, 500, 700, 1200, 2400, 4800, 9600};

        for (var range_cu : ranges_cu) {
            for (var table : pitchBendTablesFor(range_cu)) {
                table.writeCsvFile();
            }
        }
    }

    public static class PitchBendTable {
        public PitchBendTable(long rangeCu, String identifier) {
            this.rangeCu = rangeCu;
            this.identifier = identifier;

            content.append("Pitch Offset," +
                "Pitch Bend,Pitch Bend Hex,Delta Pitch Bend," +
                "Linear Pitch Offset,Linear Pitch Offset Delta,Piecewise Pitch Offset,Piecewise Pitch Offset Delta\n");
        }

        private final long rangeCu;
        private final String identifier;
        private final StringBuilder content = new StringBuilder();

        private long lastPitchBend = 0;
        private long maxPitchBendDeviation = 0;
        private double maxLinearOffsetDeviation = 0.0;
        private double maxPiecewiseOffsetDeviation = 0.0;
        private long averageCount = 0;
        private double averagePitchBendDeviation = 0.0;
        private double averageLinearOffsetDeviation = 0.0;
        private double averagePiecewiseOffsetDeviation = 0.0;

        void writeCsvFile()
        throws FileUtilsErrorException {
            var output = new StringBuilder(content);

            output.append("\"Max Deviation\"");
            output.append(SEPARATOR);
            output.append(SEPARATOR);
            output.append(SEPARATOR);
            output.append(maxPitchBendDeviation);
            output.append(SEPARATOR);
            output.append(SEPARATOR);
            output.append(String.format("%.3f", maxLinearOffsetDeviation));
            output.append(SEPARATOR);
            output.append(SEPARATOR);
            output.append(String.format("%.3f", maxPiecewiseOffsetDeviation));
            output.append("\n");

            output.append("\"Average Deviation\"");
            output.append(SEPARATOR);
            output.append(SEPARATOR);
            output.append(SEPARATOR);
            output.append(String.format("%.3f", averagePitchBendDeviation / averageCount));
            output.append(SEPARATOR);
            output.append(SEPARATOR);
            output.append(String.format("%.3f", averageLinearOffsetDeviation / averageCount));
            output.append(SEPARATOR);
            output.append(SEPARATOR);
            output.append(String.format("%.3f", averagePiecewiseOffsetDeviation / averageCount));
            output.append("\n");

            FileUtils.writeString(output.toString(), new File(FILE_PREFIX + rangeCu + "_" + identifier + FILE_EXTENSION));
        }

        void append(long offsetCentUnit, long pitchBend, double linearPitchOffset, double piecewisePitchOffset) {
            content.append(offsetCentUnit);
            content.append(SEPARATOR);
            content.append(pitchBend);
            content.append(SEPARATOR);
            content.append("\"0x");
            content.append(String.format("%1$4s", Long.toHexString(pitchBend).toUpperCase()).replace(' ', '0'));
            content.append("\"");
            content.append(SEPARATOR);
            var pitch_bend_delta = pitchBend - lastPitchBend;
            content.append(pitch_bend_delta);
            content.append(SEPARATOR);
            content.append(String.format("%.3f", linearPitchOffset));
            content.append(SEPARATOR);
            var linear_offset_delta = offsetCentUnit - linearPitchOffset;
            content.append(String.format("%.3f", linear_offset_delta));
            content.append(SEPARATOR);
            content.append(String.format("%.3f", piecewisePitchOffset));
            content.append(SEPARATOR);
            var piecewise_offset_delta = offsetCentUnit - piecewisePitchOffset;
            content.append(String.format("%.3f", piecewise_offset_delta));
            content.append("\n");

            maxPitchBendDeviation = Math.max(maxPitchBendDeviation, Math.abs(pitch_bend_delta));
            maxLinearOffsetDeviation = Math.max(maxLinearOffsetDeviation, Math.abs(linear_offset_delta));
            maxPiecewiseOffsetDeviation = Math.max(maxPiecewiseOffsetDeviation, Math.abs(piecewise_offset_delta));
            averageCount += 1;
            averagePitchBendDeviation += Math.abs(pitch_bend_delta);
            averageLinearOffsetDeviation += Math.abs(linear_offset_delta);
            averagePiecewiseOffsetDeviation += Math.abs(piecewise_offset_delta);

            lastPitchBend = pitchBend;
        }
    }

    private static List<PitchBendTable> pitchBendTablesFor(long rangeCU) {
        var linear_midi1 = new PitchBendTable(rangeCU, "linear_midi1");
        var piecewise_midi1 = new PitchBendTable(rangeCU, "piecewise_midi1");
        var linear_midi2 = new PitchBendTable(rangeCU, "linear_midi2");
        var piecewise_midi2 = new PitchBendTable(rangeCU, "piecewise_midi2");
        var scale_linear_midi1to2 = new PitchBendTable(rangeCU, "scale_linear_midi1to2");
        var scale_linear_midi1to2to1 = new PitchBendTable(rangeCU, "scale_linear_midi1to2to1");
        var scale_linear_midi2to1 = new PitchBendTable(rangeCU, "scale_linear_midi2to1");
        var scale_piecewise_midi1to2 = new PitchBendTable(rangeCU, "scale_piecewise_midi1to2");
        var scale_piecewise_midi1to2to1 = new PitchBendTable(rangeCU, "scale_piecewise_midi1to2to1");
        var scale_piecewise_midi2to1 = new PitchBendTable(rangeCU, "scale_piecewise_midi2to1");
        var mapshift_linear_midi1to2 = new PitchBendTable(rangeCU, "mapshift_linear_midi1to2");
        var mapshift_linear_midi1to2to1 = new PitchBendTable(rangeCU, "mapshift_linear_midi1to2to1");

        var tables = List.of(linear_midi1, piecewise_midi1, linear_midi2, piecewise_midi2,
            scale_linear_midi1to2, scale_linear_midi1to2to1, scale_linear_midi2to1,
            scale_piecewise_midi1to2, scale_piecewise_midi1to2to1, scale_piecewise_midi2to1,
            mapshift_linear_midi1to2, mapshift_linear_midi1to2to1);

        for (long offset_cu = -rangeCU; offset_cu <= rangeCU; ++offset_cu) {
            long linear_pitch_bend1 = calcLinearPitchBendMIDI1(offset_cu, rangeCU);
            linear_midi1.append(offset_cu,
                linear_pitch_bend1,
                calcLinearPitchMIDI1(linear_pitch_bend1, rangeCU),
                calcPiecewisePitchMIDI1(linear_pitch_bend1, rangeCU));

            long piecewise_pitch_bend1 = calcPiecewisePitchBendMIDI1(offset_cu, rangeCU);
            piecewise_midi1.append(offset_cu,
                piecewise_pitch_bend1,
                calcLinearPitchMIDI1(piecewise_pitch_bend1, rangeCU),
                calcPiecewisePitchMIDI1(piecewise_pitch_bend1, rangeCU));

            long linear_pitch_bend2 = calcLinearPitchBendMIDI2(offset_cu, rangeCU);
            linear_midi2.append(offset_cu,
                linear_pitch_bend2,
                calcLinearPitchMIDI2(linear_pitch_bend2, rangeCU),
                calcPiecewisePitchMIDI2(linear_pitch_bend2, rangeCU));

            long piecewise_pitch_bend2 = calcPiecewisePitchBendMIDI2(offset_cu, rangeCU);
            piecewise_midi2.append(offset_cu,
                piecewise_pitch_bend2,
                calcLinearPitchMIDI2(piecewise_pitch_bend2, rangeCU),
                calcPiecewisePitchMIDI2(piecewise_pitch_bend2, rangeCU));

            long linear_pitch_bend_scaled12 = scaleUp(linear_pitch_bend1, 14, 32);
            scale_linear_midi1to2.append(offset_cu,
                linear_pitch_bend_scaled12,
                calcLinearPitchMIDI2(linear_pitch_bend_scaled12, rangeCU),
                calcPiecewisePitchMIDI2(linear_pitch_bend_scaled12, rangeCU));

            long linear_pitch_bend_scaled121 = scaleDown(scaleUp(linear_pitch_bend1, 14, 32), 32, 14);
            scale_linear_midi1to2to1.append(offset_cu,
                linear_pitch_bend_scaled121,
                calcLinearPitchMIDI1(linear_pitch_bend_scaled121, rangeCU),
                calcPiecewisePitchMIDI1(linear_pitch_bend_scaled121, rangeCU));

            long linear_pitch_bend_scaled21 = scaleDown(calcLinearPitchBendMIDI2(offset_cu, rangeCU), 32, 14);
            scale_linear_midi2to1.append(offset_cu,
                linear_pitch_bend_scaled21,
                calcLinearPitchMIDI1(linear_pitch_bend_scaled21, rangeCU),
                calcPiecewisePitchMIDI1(linear_pitch_bend_scaled21, rangeCU));

            long piecewise_pitch_bend_scaled12 = scaleUp(piecewise_pitch_bend1, 14, 32);
            scale_piecewise_midi1to2.append(offset_cu,
                piecewise_pitch_bend_scaled12,
                calcLinearPitchMIDI2(piecewise_pitch_bend_scaled12, rangeCU),
                calcPiecewisePitchMIDI2(piecewise_pitch_bend_scaled12, rangeCU));

            long piecewise_pitch_bend_scaled121 = scaleDown(scaleUp(piecewise_pitch_bend1, 14, 32), 32, 14);
            scale_piecewise_midi1to2to1.append(offset_cu,
                piecewise_pitch_bend_scaled121,
                calcLinearPitchMIDI1(piecewise_pitch_bend_scaled121, rangeCU),
                calcPiecewisePitchMIDI1(piecewise_pitch_bend_scaled121, rangeCU));

            long piecewise_pitch_bend_scaled21 = scaleDown(calcPiecewisePitchBendMIDI2(offset_cu, rangeCU), 32, 14);
            scale_piecewise_midi2to1.append(offset_cu,
                piecewise_pitch_bend_scaled21,
                calcLinearPitchMIDI1(piecewise_pitch_bend_scaled21, rangeCU),
                calcPiecewisePitchMIDI1(piecewise_pitch_bend_scaled21, rangeCU));

            long linear_pitch_bend_mapshift12 = mapShift14To32(linear_pitch_bend1);
            mapshift_linear_midi1to2.append(offset_cu,
                linear_pitch_bend_mapshift12,
                calcLinearPitchMIDI2(linear_pitch_bend_mapshift12, rangeCU),
                calcPiecewisePitchMIDI2(linear_pitch_bend_mapshift12, rangeCU));

            long linear_pitch_bend_mapshift121 = scaleDown(mapShift14To32(linear_pitch_bend1), 32, 14);
            mapshift_linear_midi1to2to1.append(offset_cu,
                linear_pitch_bend_mapshift121,
                calcLinearPitchMIDI1(linear_pitch_bend_mapshift121, rangeCU),
                calcPiecewisePitchMIDI1(linear_pitch_bend_mapshift121, rangeCU));
        }

        return tables;
    }

    /**
     * This calculates pitch bend according to the formula:
     * {@code min((pitchOffset * 0x2000 / pitchSense) + 0x2000, 0x3FFF))
     * <p>
     * In order to not have to use floating point math, pitch offset and range
     * are expressed in cents.
     * <p>
     *
     * @param pitchOffsetCU the pitch offset in cents
     * @param pitchSenseCU  the pitch sensitivity in cents
     * @return the pitch bend value
     */
    public static long calcLinearPitchBendMIDI1(long pitchOffsetCU, long pitchSenseCU) {
        return min((pitchOffsetCU * 0x2000L / pitchSenseCU) + 0x2000L, 0x3FFFL);
    }

    /**
     * This calculates pitch bend with a piece-wise linear equation to account
     * for the one value asymmetry around midpoint 0x2000L.
     * <p>
     * In order to not have to use floating point math, pitch offset and range
     * are expressed in cents.
     * <p>
     *
     * @param pitchOffsetCU the pitch offset in cents
     * @param pitchSenseCU  the pitch sensitivity in cents
     * @return the pitch bend value
     */
    public static long calcPiecewisePitchBendMIDI1(long pitchOffsetCU, long pitchSenseCU) {
        if (pitchOffsetCU <= 0) {
            return pitchOffsetCU * 0x2000L / pitchSenseCU + 0x2000L;
        }
        return pitchOffsetCU * 0x1FFFL / pitchSenseCU + 0x2000L;
    }

    /**
     * This calculates pitch according to the formula:
     * {@code pitchSense * max((pitchBendValue - 0x2000) / 0x1FFF,-1))}
     * <p>
     * However, in order to not use floating point match, the formula has been
     * rearranged to:
     * {@code max(pitchSense * (pitchBendValue - 0x2000) / 0x1FFF, -pitchSense)}
     * <p>
     * In order to not have to use floating point math, pitch range
     * is expressed in cents.
     */
    public static double calcLinearPitchMIDI1(double pitchBendValue, double pitchSenseCU) {
        return max(pitchSenseCU * (pitchBendValue - 0x2000L) / 0x1FFFL, -pitchSenseCU);
    }

    /**
     * This calculates pitch with a piece-wise linear equation to account
     * for the one value asymmetry around midpoint 0x2000L.
     * <p>
     * In order to not have to use floating point math, pitch range
     * is expressed in cents.
     */
    public static double calcPiecewisePitchMIDI1(double pitchBendValue, double pitchSenseCU) {
        if (pitchBendValue <= 0x2000L) {
            return pitchSenseCU * (pitchBendValue - 0x2000L) / 0x2000L;
        }
        return pitchSenseCU * (pitchBendValue - 0x2000L) / 0x1FFFL;
    }

    /**
     * This calculates pitch bend according to the formula:
     * {@code min((pitchOffset * 0x80000000 / pitchSense) + 0x80000000, 0xFFFFFFFF))
     * <p>
     * In order to not have to use floating point math, pitch offset and range
     * are expressed in cents.
     * <p>
     *
     * @param pitchOffsetCU the pitch offset in cents
     * @param pitchSenseCU  the pitch sensitivity in cents
     * @return the pitch bend value
     */
    public static long calcLinearPitchBendMIDI2(long pitchOffsetCU, long pitchSenseCU) {
        return min((pitchOffsetCU * 0x80000000L / pitchSenseCU) + 0x80000000L, 0xFFFFFFFFL);
    }

    /**
     * This calculates pitch bend with a piece-wise linear equation to account
     * for the one value asymmetry around midpoint 0x80000000L.
     * <p>
     * In order to not have to use floating point math, pitch offset and range
     * are expressed in cents.
     *
     * @param pitchOffsetCU the pitch offset in cents
     * @param pitchSenseCU  the pitch sensitivity in cents
     * @return the pitch bend value
     */
    public static long calcPiecewisePitchBendMIDI2(long pitchOffsetCU, long pitchSenseCU) {
        if (pitchOffsetCU <= 0) {
            return pitchOffsetCU * 0x80000000L / pitchSenseCU + 0x80000000L;
        }
        return pitchOffsetCU * 0x7FFFFFFFL / pitchSenseCU + 0x80000000L;
    }

    /**
     * This calculates pitch according to the formula:
     * {@code pitchSense * max((pitchBendValue - 0x80000000) / 0x7FFFFFFF,-1))}
     * <p>
     * However, in order to not use floating point match, the formula has been
     * rearranged to:
     * {@code max(pitchSense * (pitchBendValue - 0x80000000) / 0x7FFFFFFF, -pitchSense)}
     * <p>
     * In order to not have to use floating point math, pitch range
     * is expressed in cents.
     */
    public static double calcLinearPitchMIDI2(double pitchBendValue, double pitchSenseCU) {
        return max(pitchSenseCU * (pitchBendValue - 0x80000000L) / 0x7FFFFFFFL, -pitchSenseCU);
    }

    /**
     * This calculates pitch with a piece-wise linear equation to account
     * for the one value asymmetry around midpoint 0x80000000L.
     * <p>
     * In order to not have to use floating point math, pitch range
     * is expressed in cents.
     */
    public static double calcPiecewisePitchMIDI2(double pitchBendValue, double pitchSenseCU) {
        if (pitchBendValue <= 0x80000000L) {
            return pitchSenseCU * (pitchBendValue - 0x80000000L) / 0x80000000L;
        }
        return pitchSenseCU * (pitchBendValue - 0x80000000L) / 0x7FFFFFFFL;
    }

    /**
     * This converts 14 bit MIDI pitch bend to 32 bit MIDI pitch bend by
     * mapping 0x3FFFL to 0xFFFFFFFFL and using a simple leftward
     * bitshift of 18 for everything else.
     */
    public static long mapShift14To32(long srcVal) {
        if (srcVal >= 0x3FFFL) {
            return 0xFFFFFFFFL;
        }

        return srcVal << 18;
    }

    /*
     * The formula's below are taken from the translation pseudocode in the standard document:
     * MIDI 2.0 Bit Scaling and Resolution : 3. Min-Center-Max Scaling
     */
    public static long power(long a, long b) {
        long ret = a;
        while (--b >= 1) {
            ret *= a;
        }
        return ret;
    }

    public static long scaleUp(long srcVal, int srcBits, int dstBits) {
        /* simple bit shift */
        long scaleBits = (dstBits - srcBits);
        long bitShiftedValue = srcVal << scaleBits;
        long srcCenter = power(2, srcBits - 1);
        if (srcVal <= srcCenter) {
            return bitShiftedValue;
        }
        /* expanded bit repeat scheme */
        long repeatBits = srcBits - 1;
        long repeatMask = power(2, repeatBits) - 1;
        long repeatValue = srcVal & repeatMask;
        if (scaleBits > repeatBits) {
            repeatValue <<= scaleBits - repeatBits;
        } else {
            repeatValue >>= repeatBits - scaleBits;
        }
        while (repeatValue != 0) {
            bitShiftedValue |= repeatValue;
            repeatValue >>= repeatBits;
        }
        return bitShiftedValue;
    }

    public static long scaleDown(long srcVal, int srcBits, int dstBits) {
        // simple bit shift
        int scaleBits = (srcBits - dstBits);
        return srcVal >> scaleBits;
    }
}