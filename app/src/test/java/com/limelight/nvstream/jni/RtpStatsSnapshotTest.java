package com.limelight.nvstream.jni;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RtpStatsSnapshotTest {
    @Test
    public void parsesVersionedNativeSnapshot() {
        long[] values = new long[RtpStatsSnapshot.NATIVE_FIELD_COUNT];
        values[0] = RtpStatsSnapshot.NATIVE_SCHEMA_VERSION;
        for (int i = 1; i < values.length; i++) {
            values[i] = i * 10L;
        }

        RtpStatsSnapshot snapshot = RtpStatsSnapshot.fromNative(values);

        assertTrue(snapshot.available);
        assertEquals(10L, snapshot.videoPackets);
        assertEquals(70L, snapshot.videoFecInvalid);
        assertEquals(80L, snapshot.audioPackets);
        assertEquals(140L, snapshot.audioFecInvalid);
        assertEquals(150, snapshot.pendingVideoFrames);
        assertEquals(160, snapshot.pendingAudioFrames);
        assertEquals(170, snapshot.pendingAudioDurationMs);
    }

    @Test
    public void rejectsUnknownSchemaWithoutLeakingPartialValues() {
        long[] values = new long[RtpStatsSnapshot.NATIVE_FIELD_COUNT];
        values[0] = RtpStatsSnapshot.NATIVE_SCHEMA_VERSION + 1L;
        values[1] = 1234L;

        RtpStatsSnapshot snapshot = RtpStatsSnapshot.fromNative(values);

        assertFalse(snapshot.available);
        assertEquals(0L, snapshot.videoPackets);
        assertEquals(-1, snapshot.pendingVideoFrames);
        assertEquals(-1, snapshot.pendingAudioFrames);
        assertEquals(-1, snapshot.pendingAudioDurationMs);
    }

    @Test
    public void clampsUnsignedCountersAndPendingIntegers() {
        long[] values = new long[RtpStatsSnapshot.NATIVE_FIELD_COUNT];
        values[0] = RtpStatsSnapshot.NATIVE_SCHEMA_VERSION;
        values[1] = -5L;
        values[15] = Long.MAX_VALUE;
        values[16] = Long.MIN_VALUE;
        values[17] = -1L;

        RtpStatsSnapshot snapshot = RtpStatsSnapshot.fromNative(values);

        assertEquals(0L, snapshot.videoPackets);
        assertEquals(Integer.MAX_VALUE, snapshot.pendingVideoFrames);
        assertEquals(Integer.MIN_VALUE, snapshot.pendingAudioFrames);
        assertEquals(-1, snapshot.pendingAudioDurationMs);
    }
}
