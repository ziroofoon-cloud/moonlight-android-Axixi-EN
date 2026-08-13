package com.limelight.binding.input.driver;

/**
 * Builds USB output reports for a DualSense controller.
 */
final class DualSenseOutputReport {
    static final int REPORT_SIZE = 48;
    static final int TRIGGER_PAYLOAD_SIZE = 10;

    static final int VALID_FLAG0_INDEX = 1;
    static final int VALID_FLAG1_INDEX = 2;
    static final int RIGHT_MOTOR_INDEX = 3;
    static final int LEFT_MOTOR_INDEX = 4;
    static final int RIGHT_TRIGGER_TYPE_INDEX = 11;
    static final int RIGHT_TRIGGER_DATA_INDEX = 12;
    static final int LEFT_TRIGGER_TYPE_INDEX = 22;
    static final int LEFT_TRIGGER_DATA_INDEX = 23;
    static final int PLAYER_INDICATOR_INDEX = 44;
    static final int LIGHTBAR_RED_INDEX = 45;
    static final int LIGHTBAR_GREEN_INDEX = 46;
    static final int LIGHTBAR_BLUE_INDEX = 47;

    static final byte ENABLE_RUMBLE = 0x03;
    static final byte ENABLE_RIGHT_TRIGGER = 0x04;
    static final byte ENABLE_LEFT_TRIGGER = 0x08;
    static final byte ENABLE_LIGHTBAR = 0x04;
    static final byte ENABLE_PLAYER_INDICATOR = 0x10;
    static final byte PLAYER_INDICATOR_MASK = 0x1F;

    private DualSenseOutputReport() {
    }

    /**
     * Builds a standard dual-motor rumble report.
     *
     * @param lowFreqMotor Low-frequency motor intensity.
     * @param highFreqMotor High-frequency motor intensity.
     * @return A USB DualSense output report.
     */
    static byte[] rumble(short lowFreqMotor, short highFreqMotor) {
        byte[] report = emptyReport();
        report[VALID_FLAG0_INDEX] = ENABLE_RUMBLE;
        report[RIGHT_MOTOR_INDEX] = (byte) (highFreqMotor >> 8);
        report[LEFT_MOTOR_INDEX] = (byte) (lowFreqMotor >> 8);
        return report;
    }

    /**
     * Builds an adaptive-trigger report from Sunshine's trigger effect payload.
     *
     * @param eventFlags Bitmask identifying the triggers to update.
     * @param typeLeft Left trigger effect type.
     * @param typeRight Right trigger effect type.
     * @param left Left trigger effect payload.
     * @param right Right trigger effect payload.
     * @return A USB DualSense output report.
     */
    static byte[] adaptiveTriggers(byte eventFlags, byte typeLeft, byte typeRight,
                                   byte[] left, byte[] right) {
        byte[] report = emptyReport();
        byte triggerFlags = (byte) (eventFlags & (ENABLE_LEFT_TRIGGER | ENABLE_RIGHT_TRIGGER));
        report[VALID_FLAG0_INDEX] = triggerFlags;

        if ((triggerFlags & ENABLE_RIGHT_TRIGGER) != 0) {
            report[RIGHT_TRIGGER_TYPE_INDEX] = typeRight;
            copyPayload(right, 0, report, RIGHT_TRIGGER_DATA_INDEX);
        }
        if ((triggerFlags & ENABLE_LEFT_TRIGGER) != 0) {
            report[LEFT_TRIGGER_TYPE_INDEX] = typeLeft;
            copyPayload(left, 0, report, LEFT_TRIGGER_DATA_INDEX);
        }
        return report;
    }

    /**
     * Builds a lightbar color report.
     *
     * @param red Red component.
     * @param green Green component.
     * @param blue Blue component.
     * @return A USB DualSense output report.
     */
    static byte[] lightbar(byte red, byte green, byte blue) {
        byte[] report = emptyReport();
        report[VALID_FLAG1_INDEX] = ENABLE_LIGHTBAR;
        report[LIGHTBAR_RED_INDEX] = red;
        report[LIGHTBAR_GREEN_INDEX] = green;
        report[LIGHTBAR_BLUE_INDEX] = blue;
        return report;
    }

    /**
     * Builds a native DualSense player-indicator report.
     *
     * @param playerIndicator Five-bit player LED mask.
     * @return A USB DualSense output report.
     */
    static byte[] playerIndicator(byte playerIndicator) {
        byte[] report = emptyReport();
        report[VALID_FLAG1_INDEX] = ENABLE_PLAYER_INDICATOR;
        report[PLAYER_INDICATOR_INDEX] = (byte) (playerIndicator & PLAYER_INDICATOR_MASK);
        return report;
    }

    /**
     * Builds an adaptive-trigger report from the app's legacy effect arrays.
     *
     * @param rightEffect Right effect type followed by effect data.
     * @param leftEffect Left effect type followed by effect data.
     * @return A USB DualSense output report.
     */
    static byte[] adaptiveTriggersFromLegacy(byte[] rightEffect, byte[] leftEffect) {
        byte[] report = emptyReport();
        report[VALID_FLAG0_INDEX] = ENABLE_RIGHT_TRIGGER | ENABLE_LEFT_TRIGGER;

        if (rightEffect != null && rightEffect.length > 0) {
            report[RIGHT_TRIGGER_TYPE_INDEX] = rightEffect[0];
            copyPayload(rightEffect, 1, report, RIGHT_TRIGGER_DATA_INDEX);
        }
        if (leftEffect != null && leftEffect.length > 0) {
            report[LEFT_TRIGGER_TYPE_INDEX] = leftEffect[0];
            copyPayload(leftEffect, 1, report, LEFT_TRIGGER_DATA_INDEX);
        }
        return report;
    }

    private static byte[] emptyReport() {
        byte[] report = new byte[REPORT_SIZE];
        report[0] = 0x02;
        return report;
    }

    private static void copyPayload(byte[] source, int sourceOffset,
                                    byte[] destination, int destinationOffset) {
        if (source == null || sourceOffset >= source.length) {
            return;
        }

        System.arraycopy(source, sourceOffset, destination, destinationOffset,
                Math.min(TRIGGER_PAYLOAD_SIZE, source.length - sourceOffset));
    }
}
