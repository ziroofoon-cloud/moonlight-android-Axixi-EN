package com.limelight.binding.video;

public class PerfOverlayStats {
    public int width;
    public int height;
    public int targetBitrateKbps;
    public int targetFps;
    public int networkLatencyMs;
    public int networkLatencyVarianceMs;

    public float totalFps;
    public float receivedFps;
    public float renderedFps;
    /** Video-frame loss inferred from decode-unit frame-number gaps, not raw RTP packet loss. */
    public float frameLossPercent;
    /** @deprecated Use {@link #frameLossPercent}. Kept for existing HUD formatting. */
    @Deprecated
    public float packetLossPercent;
    public float decodeTimeMs = -1f;
    public float hostProcessingLatencyMs;
    public float networkRateKbps;
    public float videoRateKbps;
    public float audioRateKbps;

    public long videoBytes;
    public long audioBytes;
    public long totalNetworkBytes;

    public long framesReceived;
    public long framesRendered;
    public long framesLost;
    public long frameLossEvents;

    public boolean hdr;
    public String codecName;
    public String decoderName;
}
