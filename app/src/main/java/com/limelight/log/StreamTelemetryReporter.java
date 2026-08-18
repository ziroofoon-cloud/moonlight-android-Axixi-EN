package com.limelight.log;

import com.limelight.binding.video.PerfOverlayStats;
import com.limelight.nvstream.jni.RtpStatsSnapshot;

import java.util.Locale;

/** Formats typed stream metrics into stable, low-volume key/value session records. */
public final class StreamTelemetryReporter {
    private final StreamSessionLogger logger;
    private final HapticsTelemetry hapticsTelemetry;

    private String lastState = "";
    private long samples;
    private long rttSamples;
    private long rttTotalMs;
    private int rttMaxMs = -1;
    private float worstFrameLossPercent;
    private PerfOverlayStats lastStats;
    private RtpStatsSnapshot lastRtpStats;

    private long previousVideoPackets;
    private long previousVideoFecPackets;
    private long previousVideoFecRecovered;
    private long previousVideoFecFailed;
    private long previousVideoOos;
    private long previousVideoInvalid;
    private long previousVideoFecInvalid;
    private long previousAudioPackets;
    private long previousAudioFecPackets;
    private long previousAudioFecRecovered;
    private long previousAudioFecFailed;
    private long previousAudioOos;
    private long previousAudioInvalid;
    private long previousAudioFecInvalid;
    private boolean havePreviousRtp;

    public StreamTelemetryReporter(StreamSessionLogger logger, HapticsTelemetry hapticsTelemetry) {
        this.logger = logger;
        this.hapticsTelemetry = hapticsTelemetry;
    }

    public synchronized void record(PerfOverlayStats stats, RtpStatsSnapshot rtpStats,
                                    int rttMs, int rttVarianceMs, long statsAgeMs,
                                    String networkLink, String state) {
        if (stats == null) {
            return;
        }
        samples++;
        if (rttMs >= 0) {
            rttSamples++;
            rttTotalMs += rttMs;
            rttMaxMs = Math.max(rttMaxMs, rttMs);
        }
        worstFrameLossPercent = Math.max(worstFrameLossPercent, stats.frameLossPercent);
        lastStats = stats;
        lastRtpStats = rtpStats != null && rtpStats.available ? rtpStats : lastRtpStats;

        logger.info("NET_SAMPLE", String.format(Locale.US,
                "size=%dx%d, codec=%s, fpsRendered=%.1f, fpsReceived=%.1f, "
                        + "networkKbps=%.0f, videoKbps=%.0f, audioKbpsEstimated=%.0f, "
                        + "rttMs=%d, rttVarMs=%d, statsAgeMs=%d, frameLossPct=%.2f, "
                        + "decodeMs=%.2f, hostMs=%.2f, framesReceived=%d, framesRendered=%d, "
                        + "framesLost=%d, lossEvents=%d, link={%s}",
                stats.width, stats.height, safe(stats.codecName), stats.renderedFps,
                stats.receivedFps, stats.networkRateKbps, stats.videoRateKbps,
                stats.audioRateKbps, rttMs, rttVarianceMs, Math.max(0L, statsAgeMs),
                stats.frameLossPercent, stats.decodeTimeMs, stats.hostProcessingLatencyMs,
                stats.framesReceived, stats.framesRendered, stats.framesLost,
                stats.frameLossEvents, safe(networkLink)));

        if (rtpStats != null && rtpStats.available) {
            logger.info("RTP_VIDEO", "packets=" + cumulativeDelta(rtpStats.videoPackets, previousVideoPackets)
                    + ", fec=" + cumulativeDelta(rtpStats.videoFecPackets, previousVideoFecPackets)
                    + ", recovered=" + cumulativeDelta(rtpStats.videoFecRecovered, previousVideoFecRecovered)
                    + ", failed=" + cumulativeDelta(rtpStats.videoFecFailed, previousVideoFecFailed)
                    + ", outOfSequence=" + cumulativeDelta(rtpStats.videoOutOfSequence, previousVideoOos)
                    + ", invalid=" + cumulativeDelta(rtpStats.videoInvalid, previousVideoInvalid)
                    + ", fecInvalid=" + cumulativeDelta(rtpStats.videoFecInvalid, previousVideoFecInvalid)
                    + ", pendingFrames=" + rtpStats.pendingVideoFrames);
            logger.info("RTP_AUDIO", "packets=" + cumulativeDelta(rtpStats.audioPackets, previousAudioPackets)
                    + ", fec=" + cumulativeDelta(rtpStats.audioFecPackets, previousAudioFecPackets)
                    + ", recovered=" + cumulativeDelta(rtpStats.audioFecRecovered, previousAudioFecRecovered)
                    + ", failed=" + cumulativeDelta(rtpStats.audioFecFailed, previousAudioFecFailed)
                    + ", outOfSequence=" + cumulativeDelta(rtpStats.audioOutOfSequence, previousAudioOos)
                    + ", invalid=" + cumulativeDelta(rtpStats.audioInvalid, previousAudioInvalid)
                    + ", fecInvalid=" + cumulativeDelta(rtpStats.audioFecInvalid, previousAudioFecInvalid)
                    + ", pendingFrames=" + rtpStats.pendingAudioFrames
                    + ", pendingMs=" + rtpStats.pendingAudioDurationMs);
            rememberRtp(rtpStats);
        }

        String safeState = safe(state);
        if (!safeState.equals(lastState)) {
            lastState = safeState;
            logger.info("STATE", safeState);
        }
        String haptics = hapticsTelemetry.snapshotAndReset();
        if (!haptics.isEmpty()) {
            logger.info("RUMBLE_STATS", haptics);
        }
    }

    public synchronized void finish(String reason, String state) {
        String haptics = hapticsTelemetry.snapshotAndReset();
        if (!haptics.isEmpty()) {
            logger.info("RUMBLE_STATS", haptics);
        }
        if (lastStats == null) {
            logger.important("SESSION_SUMMARY", "reason=" + safe(reason) + ", samples=0, state={"
                    + safe(state) + "}");
            return;
        }
        double averageRtt = rttSamples == 0L ? -1.0 : rttTotalMs / (double) rttSamples;
        RtpStatsSnapshot finalRtp = lastRtpStats;
        logger.important("SESSION_SUMMARY", String.format(Locale.US,
                "reason=%s, samples=%d, rttAvgMs=%.1f, rttMaxMs=%d, "
                        + "worstFrameLossPct=%.2f, videoBytes=%d, audioBytesEstimated=%d, "
                        + "framesReceived=%d, framesRendered=%d, framesLost=%d, lossEvents=%d, "
                        + "videoFecRecovered=%d, videoFecFailed=%d, "
                        + "audioFecRecovered=%d, audioFecFailed=%d, state={%s}",
                safe(reason), samples, averageRtt, rttMaxMs, worstFrameLossPercent,
                lastStats.videoBytes, lastStats.audioBytes, lastStats.framesReceived,
                lastStats.framesRendered, lastStats.framesLost, lastStats.frameLossEvents,
                finalRtp != null ? finalRtp.videoFecRecovered : 0L,
                finalRtp != null ? finalRtp.videoFecFailed : 0L,
                finalRtp != null ? finalRtp.audioFecRecovered : 0L,
                finalRtp != null ? finalRtp.audioFecFailed : 0L, safe(state)));
    }

    private String cumulativeDelta(long current, long previous) {
        long delta = !havePreviousRtp || current < previous ? current : current - previous;
        return current + "(delta=" + delta + ')';
    }

    private void rememberRtp(RtpStatsSnapshot stats) {
        previousVideoPackets = stats.videoPackets;
        previousVideoFecPackets = stats.videoFecPackets;
        previousVideoFecRecovered = stats.videoFecRecovered;
        previousVideoFecFailed = stats.videoFecFailed;
        previousVideoOos = stats.videoOutOfSequence;
        previousVideoInvalid = stats.videoInvalid;
        previousVideoFecInvalid = stats.videoFecInvalid;
        previousAudioPackets = stats.audioPackets;
        previousAudioFecPackets = stats.audioFecPackets;
        previousAudioFecRecovered = stats.audioFecRecovered;
        previousAudioFecFailed = stats.audioFecFailed;
        previousAudioOos = stats.audioOutOfSequence;
        previousAudioInvalid = stats.audioInvalid;
        previousAudioFecInvalid = stats.audioFecInvalid;
        havePreviousRtp = true;
    }

    private static String safe(String value) {
        return value == null || value.trim().isEmpty() ? "--"
                : value.replace('\n', ' ').replace('\r', ' ');
    }
}
