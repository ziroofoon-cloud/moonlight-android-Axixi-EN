package com.limelight.extensions.input.touch;

/**
 * EXTENSION DEVELOPMENT [EXT-TOUCHPAD-MULTI-GESTURE] [ADDED]
 *
 * Resolves the screen-space viewport used for edge following. The untransformed video rectangle
 * is content geometry, not the visible aperture after zoom, so it must not define trigger edges.
 */
final class MultiGestureViewportCalculator {
    static final int LEFT = 0;
    static final int TOP = 1;
    static final int RIGHT = 2;
    static final int BOTTOM = 3;

    private MultiGestureViewportCalculator() {
    }

    static void calculate(float coordinateWidth, float coordinateHeight,
                          float reservedBottomInset, float[] outBounds) {
        if (outBounds == null || outBounds.length < 4) {
            throw new IllegalArgumentException("outBounds must contain at least four elements");
        }

        float width = Math.max(coordinateWidth, 1f);
        float height = Math.max(coordinateHeight, 1f);
        float bottomInset = Math.max(0f, Math.min(reservedBottomInset, height - 1f));
        outBounds[LEFT] = 0f;
        outBounds[TOP] = 0f;
        outBounds[RIGHT] = width;
        outBounds[BOTTOM] = height - bottomInset;
    }
}
