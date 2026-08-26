package com.limelight.extensions.input.touch;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

/** EXTENSION DEVELOPMENT [EXT-TOUCHPAD-MULTI-GESTURE] [ADDED] */
public class MultiGestureViewportCalculatorTest {
    private final float[] bounds = new float[4];

    @Test
    public void noOcclusionUsesFullGestureViewport() {
        MultiGestureViewportCalculator.calculate(1080f, 2400f, 0f, bounds);

        assertArrayEquals(new float[] {0f, 0f, 1080f, 2400f}, bounds, 0.001f);
    }

    @Test
    public void accessoryReservationMovesOnlyBottomEdge() {
        MultiGestureViewportCalculator.calculate(1080f, 2400f, 484f, bounds);

        assertArrayEquals(new float[] {0f, 0f, 1080f, 1916f}, bounds, 0.001f);
    }

    @Test
    public void excessiveBottomReservationKeepsViewportValid() {
        MultiGestureViewportCalculator.calculate(1080f, 2400f, 3000f, bounds);

        assertArrayEquals(new float[] {0f, 0f, 1080f, 1f}, bounds, 0.001f);
    }
}
