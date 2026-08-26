package com.limelight.extensions.input.touch;

/**
 * EXTENSION DEVELOPMENT [EXT-TOUCHPAD-MULTI-GESTURE] [ADDED]
 *
 * Owns the reference plane shared by the multi-gesture mode's absolute cursor packets and local
 * edge-follow position. Keeping both users here prevents their coordinates drifting solely because
 * the Android video view has different dimensions.
 */
final class MultiGestureCursorTracker {
    static final short REFERENCE_WIDTH = 1280;
    static final short REFERENCE_HEIGHT = 720;

    private int referenceX = REFERENCE_WIDTH / 2;
    private int referenceY = REFERENCE_HEIGHT / 2;

    void move(short deltaX, short deltaY) {
        referenceX = clamp(referenceX + deltaX, 0, REFERENCE_WIDTH - 1);
        referenceY = clamp(referenceY + deltaY, 0, REFERENCE_HEIGHT - 1);
    }

    short getReferenceX() {
        return (short) referenceX;
    }

    short getReferenceY() {
        return (short) referenceY;
    }

    float getNormalizedX() {
        return referenceX / (float) (REFERENCE_WIDTH - 1);
    }

    float getNormalizedY() {
        return referenceY / (float) (REFERENCE_HEIGHT - 1);
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
