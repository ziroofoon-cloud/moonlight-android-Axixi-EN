package com.limelight.binding.input;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.limelight.nvstream.input.ControllerPacket;
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
    public void applyingGamepadEmulationPreservesPhysicalCapabilities() {
        short physicalCapabilities = MoonBridge.LI_CCAP_RUMBLE |
                MoonBridge.LI_CCAP_HAPTIC_PCM |
                MoonBridge.LI_CCAP_EMULATION_X360;

        assertEquals(MoonBridge.LI_CCAP_RUMBLE |
                        MoonBridge.LI_CCAP_HAPTIC_PCM |
                        MoonBridge.LI_CCAP_EMULATION_DS5,
                ControllerHandler.applyGamepadEmulationPreference(
                        physicalCapabilities, PreferenceConfiguration.GAMEPAD_EMULATION_DS5));
    }

    @Test
    public void onscreenDs5CapabilitiesContainOnlyAvailableFeatures() {
        short capabilities = ControllerHandler.getOnscreenControllerCapabilities(
                PreferenceConfiguration.GAMEPAD_EMULATION_DS5,
                true, true, false);

        assertEquals(MoonBridge.LI_CCAP_ANALOG_TRIGGERS |
                        MoonBridge.LI_CCAP_RUMBLE |
                        MoonBridge.LI_CCAP_ACCEL |
                        MoonBridge.LI_CCAP_EMULATION_DS5,
                capabilities);
        assertEquals(0, capabilities & (MoonBridge.LI_CCAP_TRIGGER_RUMBLE |
                MoonBridge.LI_CCAP_TOUCHPAD |
                MoonBridge.LI_CCAP_BATTERY_STATE |
                MoonBridge.LI_CCAP_RGB_LED |
                MoonBridge.LI_CCAP_HAPTIC_PCM));
    }

    @Test
    public void onscreenStickOnlyLayoutReportsOnlyPresentButtons() {
        assertEquals(ControllerPacket.LS_CLK_FLAG |
                        ControllerPacket.RS_CLK_FLAG |
                        ControllerPacket.SPECIAL_BUTTON_FLAG,
                ControllerHandler.getOnscreenControllerSupportedButtonFlags(true, true));
    }

    @Test
    public void virtualDsTouchpadDecoratesOnlyPlayerOneDsArrivals() {
        assertEquals(MoonBridge.LI_CCAP_RUMBLE | MoonBridge.LI_CCAP_TOUCHPAD,
                ControllerHandler.addVirtualDsTouchpadCapability((short) 0,
                        PreferenceConfiguration.GAMEPAD_EMULATION_DS5,
                        MoonBridge.LI_CCAP_RUMBLE));
        assertEquals(MoonBridge.LI_CCAP_RUMBLE | MoonBridge.LI_CCAP_TOUCHPAD,
                ControllerHandler.addVirtualDsTouchpadCapability((short) 0,
                        PreferenceConfiguration.GAMEPAD_EMULATION_DS4,
                        MoonBridge.LI_CCAP_RUMBLE));
        assertEquals(MoonBridge.LI_CCAP_RUMBLE,
                ControllerHandler.addVirtualDsTouchpadCapability((short) 0,
                        PreferenceConfiguration.GAMEPAD_EMULATION_X360,
                        MoonBridge.LI_CCAP_RUMBLE));
        assertEquals(MoonBridge.LI_CCAP_RUMBLE,
                ControllerHandler.addVirtualDsTouchpadCapability((short) 1,
                        PreferenceConfiguration.GAMEPAD_EMULATION_DS5,
                        MoonBridge.LI_CCAP_RUMBLE));
    }

    @Test
    public void virtualDsTouchpadAddsPlayerOneClickpadButton() {
        assertEquals(ControllerPacket.A_FLAG | ControllerPacket.TOUCHPAD_FLAG,
                ControllerHandler.addVirtualDsTouchpadButtonFlag((short) 0,
                        PreferenceConfiguration.GAMEPAD_EMULATION_DS5,
                        ControllerPacket.A_FLAG));
        assertEquals(ControllerPacket.A_FLAG,
                ControllerHandler.addVirtualDsTouchpadButtonFlag((short) 0,
                        PreferenceConfiguration.GAMEPAD_EMULATION_AUTO,
                        ControllerPacket.A_FLAG));
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

    @Test
    public void disablingDs5SpeakerPreservesHapticChannelsAndInput() {
        byte[] pcm = new byte[] {
                0x11, 0x12, 0x21, 0x22, 0x31, 0x32, 0x41, 0x42,
                0x51, 0x52, 0x61, 0x62, 0x71, 0x72, (byte) 0x81, (byte) 0x82
        };
        byte[] original = pcm.clone();

        assertArrayEquals(new byte[] {
                        0, 0, 0, 0, 0x31, 0x32, 0x41, 0x42,
                        0, 0, 0, 0, 0x71, 0x72, (byte) 0x81, (byte) 0x82
                },
                ControllerHandler.copyNativePcmWithoutSpeaker(pcm));
        assertArrayEquals(original, pcm);
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
