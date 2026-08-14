package com.limelight.binding.video;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VideoStatsTest {
    @Test
    public void decoderSamplesSurviveWindowAggregation() {
        VideoStats first = new VideoStats();
        first.decoderTimeUs = 1250;
        first.decoderTimeSamples = 1;

        VideoStats second = new VideoStats();
        second.decoderTimeUs = 2750;
        second.decoderTimeSamples = 2;

        VideoStats aggregate = new VideoStats();
        aggregate.add(first);
        aggregate.add(second);

        assertEquals(4000, aggregate.decoderTimeUs);
        assertEquals(3, aggregate.decoderTimeSamples);
    }

    @Test
    public void decoderSamplesCopyAndClearWithWindow() {
        VideoStats source = new VideoStats();
        source.decoderTimeUs = 950;
        source.decoderTimeSamples = 4;

        VideoStats copy = new VideoStats();
        copy.copy(source);
        assertEquals(950, copy.decoderTimeUs);
        assertEquals(4, copy.decoderTimeSamples);

        copy.clear();
        assertEquals(0, copy.decoderTimeUs);
        assertEquals(0, copy.decoderTimeSamples);
    }
}
