package com.limelight.binding.input;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.limelight.nvstream.jni.MoonBridge;
import com.limelight.preferences.PreferenceConfiguration;

import org.junit.Test;

public class ControllerHandlerTest {
    @Test
    public void mapsGamepadEmulationPreferencesToCapabilityBits() {
        assertEquals(MoonBridge.LI_CCAP_EMULATION_AUTO,
                ControllerHandler.getGamepadEmulationCapabilities(null));
        assertEquals(MoonBridge.LI_CCAP_EMULATION_X360,
                ControllerHandler.getGamepadEmulationCapabilities(
                        PreferenceConfiguration.GAMEPAD_EMULATION_X360));
        assertEquals(MoonBridge.LI_CCAP_EMULATION_DS4,
                ControllerHandler.getGamepadEmulationCapabilities(
                        PreferenceConfiguration.GAMEPAD_EMULATION_DS4));
        assertEquals(MoonBridge.LI_CCAP_EMULATION_DS5,
                ControllerHandler.getGamepadEmulationCapabilities(
                        PreferenceConfiguration.GAMEPAD_EMULATION_DS5));
    }

    @Test
    public void invalidGamepadEmulationPreferenceFallsBackToAuto() {
        assertEquals(MoonBridge.LI_CCAP_EMULATION_AUTO,
                ControllerHandler.getGamepadEmulationCapabilities("invalid"));
    }

    @Test
    public void downsampleNativePcmUsesHapticChannelsAndRetainsWindowState() {
        ControllerHandler.NativePcmDownsampler downsampler =
                new ControllerHandler.NativePcmDownsampler();

        assertArrayEquals(new byte[0], downsampler.convert(
                nativePcmFrames(10, 1000, -2000)));
        assertArrayEquals(new byte[] {(byte) 0xE8, 0x03, 0x30, (byte) 0xF8},
                downsampler.convert(nativePcmFrames(6, 1000, -2000)));
    }

    private static byte[] nativePcmFrames(int frames, int leftHaptic, int rightHaptic) {
        byte[] pcm = new byte[frames * 8];
        for (int frame = 0; frame < frames; frame++) {
            int offset = frame * 8;
            pcm[offset] = 0x55;
            pcm[offset + 2] = 0x66;
            pcm[offset + 4] = (byte) leftHaptic;
            pcm[offset + 5] = (byte) (leftHaptic >> 8);
            pcm[offset + 6] = (byte) rightHaptic;
            pcm[offset + 7] = (byte) (rightHaptic >> 8);
        }
        return pcm;
    }
}
