package com.limelight.extensions.input.touch;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

/** EXTENSION DEVELOPMENT [EXT-TOUCHPAD-MULTI-GESTURE] [ADDED] */
public class MultiGestureEdgePanCalculatorTest {
    private final float[] delta = new float[2];

    @Test
    public void cursorInsideSafeAreaDoesNotPan() {
        calculate(500f, 300f);

        assertArrayEquals(new float[] {0f, 0f}, delta, 0.001f);
    }

    @Test
    public void cursorNearRightEdgePansVideoLeft() {
        calculate(990f, 300f);

        assertArrayEquals(new float[] {-22f, 0f}, delta, 0.001f);
    }

    @Test
    public void cursorNearTopLeftCornerPansVideoDownAndRight() {
        calculate(10f, 12f);

        assertArrayEquals(new float[] {22f, 20f}, delta, 0.001f);
    }

    @Test
    public void cursorNearBottomEdgePansVideoUp() {
        calculate(500f, 595f);

        assertArrayEquals(new float[] {0f, -27f}, delta, 0.001f);
    }

    @Test
    public void originalVideoEdgeIsNotAViewportEdgeAfterZoom() {
        MultiGestureEdgePanCalculator.calculate(
                500f, 650f,
                0f, 0f, 1000f, 2000f,
                -200f, -100f, 1200f, 2100f,
                1f, delta);

        assertArrayEquals(new float[] {0f, 0f}, delta, 0.001f);
    }

    @Test
    public void oneDpInsetWaitsUntilCursorReachesBottomEdge() {
        MultiGestureEdgePanCalculator.calculate(
                500f, 598f,
                0f, 0f, 1000f, 600f,
                -200f, -100f, 1200f, 700f,
                1f, delta);

        assertArrayEquals(new float[] {0f, 0f}, delta, 0.001f);

        MultiGestureEdgePanCalculator.calculate(
                500f, 600f,
                0f, 0f, 1000f, 600f,
                -200f, -100f, 1200f, 700f,
                1f, delta);

        assertArrayEquals(new float[] {0f, -1f}, delta, 0.001f);
    }

    @Test
    public void imeReducedViewportUsesAccessoryBarTopAsBottomEdge() {
        MultiGestureEdgePanCalculator.calculate(
                500f, 395f,
                0f, 0f, 1000f, 400f,
                -200f, -100f, 1200f, 600f,
                32f, delta);

        assertArrayEquals(new float[] {0f, -27f}, delta, 0.001f);
    }

    @Test
    public void cursorAtUnoccludedLeftEdgeDoesNotPan() {
        MultiGestureEdgePanCalculator.calculate(
                10f, 300f,
                0f, 0f, 1000f, 600f,
                0f, -100f, 1200f, 700f,
                32f, delta);

        assertArrayEquals(new float[] {0f, 0f}, delta, 0.001f);
    }

    @Test
    public void cursorAtUnoccludedRightEdgeDoesNotPan() {
        MultiGestureEdgePanCalculator.calculate(
                990f, 300f,
                0f, 0f, 1000f, 600f,
                -200f, -100f, 1000f, 700f,
                32f, delta);

        assertArrayEquals(new float[] {0f, 0f}, delta, 0.001f);
    }

    @Test
    public void panDeltaDoesNotExceedHiddenContentDistance() {
        MultiGestureEdgePanCalculator.calculate(
                10f, 300f,
                0f, 0f, 1000f, 600f,
                -5f, -100f, 1200f, 700f,
                32f, delta);

        assertArrayEquals(new float[] {5f, 0f}, delta, 0.001f);
    }

    private void calculate(float cursorX, float cursorY) {
        MultiGestureEdgePanCalculator.calculate(
                cursorX, cursorY,
                0f, 0f, 1000f, 600f,
                -200f, -100f, 1200f, 700f,
                32f, delta);
    }
}
