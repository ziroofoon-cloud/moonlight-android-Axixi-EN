package com.limelight.extensions.input.touch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/** EXTENSION DEVELOPMENT [EXT-TOUCHPAD-MULTI-GESTURE] [ADDED] */
public class MultiGestureTouchContextTest {
    private RecordingListener listener;
    private MultiGestureTouchContext context;

    @Before
    public void setUp() {
        listener = new RecordingListener();
        context = new MultiGestureTouchContext(listener,
                8f, 8f, 12f, 0.05f, 250);
    }

    @Test
    public void singleFingerTapSendsLeftClick() {
        event(MultiGestureTouchContext.ACTION_DOWN, 0, 1, 0, xy(100), xy(100));
        event(MultiGestureTouchContext.ACTION_UP, 0, 1, 100, xy(103), xy(104));

        assertEquals(1, listener.leftClicks);
        assertEquals(0, listener.rightClicks);
    }

    @Test
    public void singleFingerMoveMovesPointerWithoutClicking() {
        event(MultiGestureTouchContext.ACTION_DOWN, 0, 1, 0, xy(100), xy(100));
        event(MultiGestureTouchContext.ACTION_MOVE, 0, 1, 30, xy(120), xy(130));
        event(MultiGestureTouchContext.ACTION_UP, 0, 1, 80, xy(120), xy(130));

        assertEquals(1, listener.moves.size());
        assertEquals(20f, listener.moves.get(0)[0], 0.001f);
        assertEquals(30f, listener.moves.get(0)[1], 0.001f);
        assertEquals(0, listener.leftClicks);
    }

    @Test
    public void twoFingerTapSendsRightClickWhenPrimaryFingerLiftsFirst() {
        performTwoFingerTap(0);

        assertEquals(1, listener.rightClicks);
        assertEquals(0, listener.leftClicks);
    }

    @Test
    public void twoFingerTapSendsRightClickWhenSecondaryFingerLiftsFirst() {
        performTwoFingerTap(1);

        assertEquals(1, listener.rightClicks);
        assertEquals(0, listener.leftClicks);
    }

    @Test
    public void parallelTwoFingerVerticalMovementScrollsWithoutZoomingOrClicking() {
        beginTwoFingerGesture();
        event(MultiGestureTouchContext.ACTION_MOVE, 0, 2, 50,
                xy(100, 200), xy(125, 125));
        event(MultiGestureTouchContext.ACTION_MOVE, 0, 2, 70,
                xy(100, 200), xy(135, 135));
        finishTwoFingerGesture(100);

        assertEquals(2, listener.scrolls.size());
        assertEquals(25f, listener.scrolls.get(0), 0.001f);
        assertEquals(10f, listener.scrolls.get(1), 0.001f);
        assertTrue(listener.zooms.isEmpty());
        assertEquals(0, listener.rightClicks);
    }

    @Test
    public void spreadingTwoFingersZoomsInWithoutScrolling() {
        beginTwoFingerGesture();
        event(MultiGestureTouchContext.ACTION_MOVE, 0, 2, 50,
                xy(85, 215), xy(100, 100));

        assertEquals(1, listener.zooms.size());
        assertEquals(1.3f, listener.zooms.get(0)[0], 0.001f);
        assertTrue(listener.scrolls.isEmpty());
    }

    @Test
    public void pinchingTwoFingersZoomsOutWithoutScrolling() {
        beginTwoFingerGesture();
        event(MultiGestureTouchContext.ACTION_MOVE, 0, 2, 50,
                xy(115, 185), xy(100, 100));

        assertEquals(1, listener.zooms.size());
        assertEquals(0.7f, listener.zooms.get(0)[0], 0.001f);
        assertTrue(listener.scrolls.isEmpty());
    }

    @Test
    public void oneRemainingFingerIsIgnoredUntilGestureFullyEnds() {
        beginTwoFingerGesture();
        event(MultiGestureTouchContext.ACTION_MOVE, 0, 2, 50,
                xy(100, 200), xy(120, 120));
        event(MultiGestureTouchContext.ACTION_POINTER_UP, 0, 2, 70,
                xy(100, 200), xy(120, 120));
        event(MultiGestureTouchContext.ACTION_MOVE, 0, 1, 80,
                xy(240), xy(240));
        event(MultiGestureTouchContext.ACTION_UP, 0, 1, 100,
                xy(240), xy(240));

        assertTrue(listener.moves.isEmpty());
        assertEquals(0, listener.leftClicks);
        assertEquals(0, listener.rightClicks);
    }

    private void performTwoFingerTap(int liftedPointerIndex) {
        beginTwoFingerGesture();
        event(MultiGestureTouchContext.ACTION_POINTER_UP, liftedPointerIndex, 2, 80,
                xy(100, 200), xy(100, 100));
        event(MultiGestureTouchContext.ACTION_UP, 0, 1, 120,
                xy(liftedPointerIndex == 0 ? 200 : 100), xy(100));
    }

    private void beginTwoFingerGesture() {
        event(MultiGestureTouchContext.ACTION_DOWN, 0, 1, 0,
                xy(100), xy(100));
        event(MultiGestureTouchContext.ACTION_POINTER_DOWN, 1, 2, 20,
                xy(100, 200), xy(100, 100));
    }

    private void finishTwoFingerGesture(long eventTime) {
        event(MultiGestureTouchContext.ACTION_POINTER_UP, 1, 2, eventTime,
                xy(100, 200), xy(135, 135));
        event(MultiGestureTouchContext.ACTION_UP, 0, 1, eventTime + 20,
                xy(100), xy(135));
    }

    private void event(int action, int actionIndex, int pointerCount, long eventTime,
                       float[] x, float[] y) {
        assertTrue(context.onTouchEvent(action, actionIndex, pointerCount, x, y, eventTime));
    }

    private static float[] xy(float... values) {
        return values;
    }

    private static final class RecordingListener implements MultiGestureTouchContext.Listener {
        final List<float[]> moves = new ArrayList<>();
        final List<Float> scrolls = new ArrayList<>();
        final List<float[]> zooms = new ArrayList<>();
        int leftClicks;
        int rightClicks;

        @Override
        public void onPointerMove(float deltaX, float deltaY) {
            moves.add(new float[] {deltaX, deltaY});
        }

        @Override
        public void onVerticalScroll(float deltaY) {
            scrolls.add(deltaY);
        }

        @Override
        public void onZoom(float scaleFactor, float focusX, float focusY) {
            zooms.add(new float[] {scaleFactor, focusX, focusY});
        }

        @Override
        public void onLeftClick() {
            leftClicks++;
        }

        @Override
        public void onRightClick() {
            rightClicks++;
        }
    }
}
