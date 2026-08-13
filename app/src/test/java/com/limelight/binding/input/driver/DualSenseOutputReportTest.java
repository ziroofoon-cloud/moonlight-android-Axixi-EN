package com.limelight.binding.input.driver;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class DualSenseOutputReportTest {
    @Test
    public void initializationEnablesInternalSpeakerAtAudibleVolume() {
        byte[] report = DualSenseOutputReport.initialization();

        assertEquals(DualSenseOutputReport.REPORT_SIZE, report.length);
        assertEquals(0x02, report[0]);
        assertEquals(DualSenseOutputReport.ENABLE_AUDIO_CONFIGURATION,
                report[DualSenseOutputReport.VALID_FLAG0_INDEX]);
        assertEquals(DualSenseOutputReport.ENABLE_INITIAL_EFFECTS,
                report[DualSenseOutputReport.VALID_FLAG1_INDEX]);
        assertEquals(DualSenseOutputReport.DEFAULT_HEADPHONE_VOLUME,
                report[DualSenseOutputReport.HEADPHONE_VOLUME_INDEX]);
        assertEquals(DualSenseOutputReport.DEFAULT_SPEAKER_VOLUME,
                report[DualSenseOutputReport.SPEAKER_VOLUME_INDEX]);
        assertEquals(DualSenseOutputReport.DEFAULT_MICROPHONE_VOLUME,
                report[DualSenseOutputReport.MICROPHONE_VOLUME_INDEX]);
        assertEquals(DualSenseOutputReport.INTERNAL_SPEAKER_AUDIO_ROUTE,
                report[DualSenseOutputReport.AUDIO_ROUTE_INDEX]);
    }

    @Test
    public void rumbleMapsMotorIntensitiesWithoutEnablingOtherOutputs() {
        byte[] report = DualSenseOutputReport.rumble((short) 0xABCD, (short) 0x1234);

        assertEquals(DualSenseOutputReport.REPORT_SIZE, report.length);
        assertEquals(0x02, report[0]);
        assertEquals(DualSenseOutputReport.ENABLE_RUMBLE,
                report[DualSenseOutputReport.VALID_FLAG0_INDEX]);
        assertEquals(0, report[DualSenseOutputReport.VALID_FLAG1_INDEX]);
        assertEquals((byte) 0x12, report[DualSenseOutputReport.RIGHT_MOTOR_INDEX]);
        assertEquals((byte) 0xAB, report[DualSenseOutputReport.LEFT_MOTOR_INDEX]);
    }

    @Test
    public void adaptiveTriggersPreserveBothCompletePayloads() {
        byte[] left = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        byte[] right = {10, 11, 12, 13, 14, 15, 16, 17, 18, 19};

        byte[] report = DualSenseOutputReport.adaptiveTriggers(
                (byte) (DualSenseOutputReport.ENABLE_LEFT_TRIGGER |
                        DualSenseOutputReport.ENABLE_RIGHT_TRIGGER),
                (byte) 0x21, (byte) 0x22, left, right);

        assertEquals(0x0C, report[DualSenseOutputReport.VALID_FLAG0_INDEX]);
        assertEquals((byte) 0x21, report[DualSenseOutputReport.LEFT_TRIGGER_TYPE_INDEX]);
        assertEquals((byte) 0x22, report[DualSenseOutputReport.RIGHT_TRIGGER_TYPE_INDEX]);
        assertArrayEquals(left, Arrays.copyOfRange(report,
                DualSenseOutputReport.LEFT_TRIGGER_DATA_INDEX,
                DualSenseOutputReport.LEFT_TRIGGER_DATA_INDEX +
                        DualSenseOutputReport.TRIGGER_PAYLOAD_SIZE));
        assertArrayEquals(right, Arrays.copyOfRange(report,
                DualSenseOutputReport.RIGHT_TRIGGER_DATA_INDEX,
                DualSenseOutputReport.RIGHT_TRIGGER_DATA_INDEX +
                        DualSenseOutputReport.TRIGGER_PAYLOAD_SIZE));
    }

    @Test
    public void adaptiveTriggersOnlyPopulateRequestedTrigger() {
        byte[] left = {1, 2, 3};
        byte[] right = {4, 5, 6};

        byte[] report = DualSenseOutputReport.adaptiveTriggers(
                DualSenseOutputReport.ENABLE_RIGHT_TRIGGER,
                (byte) 0x31, (byte) 0x32, left, right);

        assertEquals(DualSenseOutputReport.ENABLE_RIGHT_TRIGGER,
                report[DualSenseOutputReport.VALID_FLAG0_INDEX]);
        assertEquals(0, report[DualSenseOutputReport.LEFT_TRIGGER_TYPE_INDEX]);
        assertEquals((byte) 0x32, report[DualSenseOutputReport.RIGHT_TRIGGER_TYPE_INDEX]);
        assertArrayEquals(new byte[DualSenseOutputReport.TRIGGER_PAYLOAD_SIZE],
                Arrays.copyOfRange(report,
                        DualSenseOutputReport.LEFT_TRIGGER_DATA_INDEX,
                        DualSenseOutputReport.LEFT_TRIGGER_DATA_INDEX +
                                DualSenseOutputReport.TRIGGER_PAYLOAD_SIZE));
        assertArrayEquals(new byte[] {4, 5, 6}, Arrays.copyOfRange(report,
                DualSenseOutputReport.RIGHT_TRIGGER_DATA_INDEX,
                DualSenseOutputReport.RIGHT_TRIGGER_DATA_INDEX + 3));
    }

    @Test
    public void lightbarOnlyEnablesRgbUpdate() {
        byte[] report = DualSenseOutputReport.lightbar(
                (byte) 0x9A, (byte) 0xBC, (byte) 0xDE);

        assertEquals(0, report[DualSenseOutputReport.VALID_FLAG0_INDEX]);
        assertEquals(DualSenseOutputReport.ENABLE_LIGHTBAR,
                report[DualSenseOutputReport.VALID_FLAG1_INDEX]);
        assertEquals((byte) 0x9A, report[DualSenseOutputReport.LIGHTBAR_RED_INDEX]);
        assertEquals((byte) 0xBC, report[DualSenseOutputReport.LIGHTBAR_GREEN_INDEX]);
        assertEquals((byte) 0xDE, report[DualSenseOutputReport.LIGHTBAR_BLUE_INDEX]);
    }

    @Test
    public void playerIndicatorOnlyEnablesPlayerLedUpdateAndMasksReservedBits() {
        byte[] report = DualSenseOutputReport.playerIndicator((byte) 0xF5);

        assertEquals(0, report[DualSenseOutputReport.VALID_FLAG0_INDEX]);
        assertEquals(DualSenseOutputReport.ENABLE_PLAYER_INDICATOR,
                report[DualSenseOutputReport.VALID_FLAG1_INDEX]);
        assertEquals(0x15, report[DualSenseOutputReport.PLAYER_INDICATOR_INDEX]);
        assertEquals(0, report[DualSenseOutputReport.LIGHTBAR_RED_INDEX]);
        assertEquals(0, report[DualSenseOutputReport.LIGHTBAR_GREEN_INDEX]);
        assertEquals(0, report[DualSenseOutputReport.LIGHTBAR_BLUE_INDEX]);
    }

    @Test
    public void legacyTriggerEffectsAreSafelyPadded() {
        byte[] report = DualSenseOutputReport.adaptiveTriggersFromLegacy(
                new byte[] {6, 10, 20, 30}, new byte[] {1, 40, 50, 60});

        assertEquals(6, report[DualSenseOutputReport.RIGHT_TRIGGER_TYPE_INDEX]);
        assertEquals(1, report[DualSenseOutputReport.LEFT_TRIGGER_TYPE_INDEX]);
        assertArrayEquals(new byte[] {10, 20, 30, 0}, Arrays.copyOfRange(report,
                DualSenseOutputReport.RIGHT_TRIGGER_DATA_INDEX,
                DualSenseOutputReport.RIGHT_TRIGGER_DATA_INDEX + 4));
        assertArrayEquals(new byte[] {40, 50, 60, 0}, Arrays.copyOfRange(report,
                DualSenseOutputReport.LEFT_TRIGGER_DATA_INDEX,
                DualSenseOutputReport.LEFT_TRIGGER_DATA_INDEX + 4));
    }
}
