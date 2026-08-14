package com.limelight.nvstream.jni;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class MoonBridgeVideoFormatTest {
    @Test
    public void tenBitMaskMatchesMoonlightCommonFormats() {
        assertEquals(0, MoonBridge.VIDEO_FORMAT_H265 & MoonBridge.VIDEO_FORMAT_MASK_10BIT);
        assertNotEquals(0, MoonBridge.VIDEO_FORMAT_H265_MAIN10 & MoonBridge.VIDEO_FORMAT_MASK_10BIT);
        assertNotEquals(0, MoonBridge.VIDEO_FORMAT_H265_REXT10_444 & MoonBridge.VIDEO_FORMAT_MASK_10BIT);
        assertNotEquals(0, MoonBridge.VIDEO_FORMAT_AV1_MAIN10 & MoonBridge.VIDEO_FORMAT_MASK_10BIT);
        assertNotEquals(0, MoonBridge.VIDEO_FORMAT_AV1_HIGH10_444 & MoonBridge.VIDEO_FORMAT_MASK_10BIT);
    }

    @Test
    public void yuv444MaskMatchesEveryExtendedFormat() {
        assertNotEquals(0, MoonBridge.VIDEO_FORMAT_H264_HIGH8_444 & MoonBridge.VIDEO_FORMAT_MASK_YUV444);
        assertNotEquals(0, MoonBridge.VIDEO_FORMAT_H265_REXT8_444 & MoonBridge.VIDEO_FORMAT_MASK_YUV444);
        assertNotEquals(0, MoonBridge.VIDEO_FORMAT_H265_REXT10_444 & MoonBridge.VIDEO_FORMAT_MASK_YUV444);
        assertNotEquals(0, MoonBridge.VIDEO_FORMAT_AV1_HIGH8_444 & MoonBridge.VIDEO_FORMAT_MASK_YUV444);
        assertNotEquals(0, MoonBridge.VIDEO_FORMAT_AV1_HIGH10_444 & MoonBridge.VIDEO_FORMAT_MASK_YUV444);
    }
}
