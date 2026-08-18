package com.limelight.nvstream.jni;

/** Immutable cumulative RTP/FEC counters copied from moonlight-common-c. */
public final class RtpStatsSnapshot {
    static final int NATIVE_SCHEMA_VERSION = 1;
    static final int NATIVE_FIELD_COUNT = 18;

    public final boolean available;

    public final long videoPackets;
    public final long videoFecPackets;
    public final long videoFecRecovered;
    public final long videoFecFailed;
    public final long videoOutOfSequence;
    public final long videoInvalid;
    public final long videoFecInvalid;

    public final long audioPackets;
    public final long audioFecPackets;
    public final long audioFecRecovered;
    public final long audioFecFailed;
    public final long audioOutOfSequence;
    public final long audioInvalid;
    public final long audioFecInvalid;

    public final int pendingVideoFrames;
    public final int pendingAudioFrames;
    public final int pendingAudioDurationMs;

    private RtpStatsSnapshot(boolean available, long[] values) {
        this.available = available;
        videoPackets = value(values, 1);
        videoFecPackets = value(values, 2);
        videoFecRecovered = value(values, 3);
        videoFecFailed = value(values, 4);
        videoOutOfSequence = value(values, 5);
        videoInvalid = value(values, 6);
        videoFecInvalid = value(values, 7);
        audioPackets = value(values, 8);
        audioFecPackets = value(values, 9);
        audioFecRecovered = value(values, 10);
        audioFecFailed = value(values, 11);
        audioOutOfSequence = value(values, 12);
        audioInvalid = value(values, 13);
        audioFecInvalid = value(values, 14);
        pendingVideoFrames = intValue(values, 15);
        pendingAudioFrames = intValue(values, 16);
        pendingAudioDurationMs = intValue(values, 17);
    }

    static RtpStatsSnapshot fromNative(long[] values) {
        boolean valid = values != null && values.length >= NATIVE_FIELD_COUNT
                && values[0] == NATIVE_SCHEMA_VERSION;
        return new RtpStatsSnapshot(valid, valid ? values : null);
    }

    static RtpStatsSnapshot unavailable() {
        return new RtpStatsSnapshot(false, null);
    }

    private static long value(long[] values, int index) {
        return values == null ? 0L : Math.max(0L, values[index]);
    }

    private static int intValue(long[] values, int index) {
        if (values == null) {
            return -1;
        }
        return (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, values[index]));
    }
}
