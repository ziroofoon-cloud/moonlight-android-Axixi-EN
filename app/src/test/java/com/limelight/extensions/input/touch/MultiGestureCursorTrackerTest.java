package com.limelight.extensions.input.touch;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** EXTENSION DEVELOPMENT [EXT-TOUCHPAD-MULTI-GESTURE] [ADDED] */
public class MultiGestureCursorTrackerTest {
    @Test
    public void startsAtCenterOfReferencePlane() {
        MultiGestureCursorTracker tracker = new MultiGestureCursorTracker();

        assertEquals(640, tracker.getReferenceX());
        assertEquals(360, tracker.getReferenceY());
        assertEquals(640f / 1279f, tracker.getNormalizedX(), 0.001f);
        assertEquals(360f / 719f, tracker.getNormalizedY(), 0.001f);
    }

    @Test
    public void movementUsesSharedAbsoluteReferencePlane() {
        MultiGestureCursorTracker tracker = new MultiGestureCursorTracker();

        tracker.move((short) 320, (short) -180);

        assertEquals(960, tracker.getReferenceX());
        assertEquals(180, tracker.getReferenceY());
        assertEquals(960f / 1279f, tracker.getNormalizedX(), 0.001f);
        assertEquals(180f / 719f, tracker.getNormalizedY(), 0.001f);
    }

    @Test
    public void movementClampsAtReferencePlaneEdges() {
        MultiGestureCursorTracker tracker = new MultiGestureCursorTracker();

        tracker.move(Short.MAX_VALUE, Short.MIN_VALUE);

        assertEquals(1279, tracker.getReferenceX());
        assertEquals(0, tracker.getReferenceY());
        assertEquals(1f, tracker.getNormalizedX(), 0.001f);
        assertEquals(0f, tracker.getNormalizedY(), 0.001f);
    }
}
