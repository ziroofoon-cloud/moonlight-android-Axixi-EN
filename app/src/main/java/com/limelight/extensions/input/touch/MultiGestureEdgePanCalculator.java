package com.limelight.extensions.input.touch;

/**
 * EXTENSION DEVELOPMENT [EXT-TOUCHPAD-MULTI-GESTURE] [ADDED]
 *
 * Calculates the video-pan delta required to keep the tracked remote cursor inside a local
 * viewport edge inset. VideoZoomController remains responsible for clamping at content bounds.
 */
final class MultiGestureEdgePanCalculator {
    private MultiGestureEdgePanCalculator() {
    }

    static void calculate(float cursorX, float cursorY,
                          float viewportLeft, float viewportTop,
                          float viewportRight, float viewportBottom,
                          float contentLeft, float contentTop,
                          float contentRight, float contentBottom,
                          float edgeInset, float[] outDelta) {
        if (outDelta == null || outDelta.length < 2) {
            throw new IllegalArgumentException("outDelta must contain at least two elements");
        }

        outDelta[0] = calculateAxis(
                cursorX, viewportLeft, viewportRight, contentLeft, contentRight, edgeInset);
        outDelta[1] = calculateAxis(
                cursorY, viewportTop, viewportBottom, contentTop, contentBottom, edgeInset);
    }

    private static float calculateAxis(float cursor, float viewportStart, float viewportEnd,
                                       float contentStart, float contentEnd, float edgeInset) {
        float viewportSize = viewportEnd - viewportStart;
        if (viewportSize <= 0f || edgeInset <= 0f) {
            return 0f;
        }

        float inset = Math.min(edgeInset, viewportSize * 0.5f);
        float nearEdge = viewportStart + inset;
        if (cursor < nearEdge) {
            float hiddenDistance = viewportStart - contentStart;
            return hiddenDistance > 0f
                    ? Math.min(nearEdge - cursor, hiddenDistance)
                    : 0f;
        }

        float farEdge = viewportEnd - inset;
        if (cursor > farEdge) {
            float hiddenDistance = contentEnd - viewportEnd;
            return hiddenDistance > 0f
                    ? Math.max(farEdge - cursor, -hiddenDistance)
                    : 0f;
        }
        return 0f;
    }
}
