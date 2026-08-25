package com.limelight.extensions.input.touch;

/**
 * EXTENSION DEVELOPMENT [EXT-TOUCHPAD-MULTI-GESTURE] [ADDED]
 *
 * A platform-independent gesture state machine for the multi-gesture touchpad mode. Android's
 * pointer events are adapted by {@link MultiGestureTouchpadExtensionController}; keeping the
 * classifier independent makes gesture precedence and release-order handling directly testable.
 */
final class MultiGestureTouchContext {
    // Values intentionally match android.view.MotionEvent action constants.
    static final int ACTION_DOWN = 0;
    static final int ACTION_UP = 1;
    static final int ACTION_MOVE = 2;
    static final int ACTION_CANCEL = 3;
    static final int ACTION_POINTER_DOWN = 5;
    static final int ACTION_POINTER_UP = 6;

    interface Listener {
        void onPointerMove(float deltaX, float deltaY);
        void onVerticalScroll(float deltaY);
        void onZoom(float scaleFactor, float focusX, float focusY);
        void onLeftClick();
        void onRightClick();
    }

    private enum State {
        IDLE,
        SINGLE,
        TWO_UNDECIDED,
        TWO_SCROLL,
        TWO_PINCH,
        WAIT_FOR_ALL_UP
    }

    private final Listener listener;
    private final float tapMovementSlop;
    private final float scrollActivationDistance;
    private final float pinchActivationDistance;
    private final float pinchActivationRatio;
    private final long tapTimeoutMs;

    private State state = State.IDLE;
    private long gestureDownTime;
    private float singleStartX;
    private float singleStartY;
    private float singleLastX;
    private float singleLastY;
    private float singleMaximumTravel;
    private final float[] twoStartX = new float[2];
    private final float[] twoStartY = new float[2];
    private float twoMaximumTravel;
    private float twoInitialSpan;
    private float twoLastSpan;
    private float twoInitialFocusX;
    private float twoInitialFocusY;
    private float twoLastFocusY;
    private boolean pendingTwoFingerTap;

    MultiGestureTouchContext(Listener listener,
                             float tapMovementSlop,
                             float scrollActivationDistance,
                             float pinchActivationDistance,
                             float pinchActivationRatio,
                             long tapTimeoutMs) {
        this.listener = listener;
        this.tapMovementSlop = tapMovementSlop;
        this.scrollActivationDistance = scrollActivationDistance;
        this.pinchActivationDistance = pinchActivationDistance;
        this.pinchActivationRatio = pinchActivationRatio;
        this.tapTimeoutMs = tapTimeoutMs;
    }

    /**
     * Consumes a normalized pointer frame. Only the first two coordinates are required when more
     * than two pointers are present, because such gestures are deliberately ignored here.
     */
    boolean onTouchEvent(int action, int actionIndex, int pointerCount,
                         float[] x, float[] y, long eventTime) {
        int requiredCoordinates = Math.min(pointerCount, 2);
        if (pointerCount < 1 || x == null || y == null
                || x.length < requiredCoordinates || y.length < requiredCoordinates) {
            cancel();
            return false;
        }

        switch (action) {
            case ACTION_DOWN:
                beginSingle(x[0], y[0], eventTime);
                return true;

            case ACTION_POINTER_DOWN:
                if (pointerCount == 2) {
                    beginTwoFinger(x, y);
                }
                else {
                    state = State.WAIT_FOR_ALL_UP;
                    pendingTwoFingerTap = false;
                }
                return true;

            case ACTION_MOVE:
                if (state == State.SINGLE && pointerCount == 1) {
                    updateSingle(x[0], y[0]);
                }
                else if (pointerCount >= 2 && isTwoFingerState()) {
                    updateTwoFinger(x, y);
                }
                return true;

            case ACTION_POINTER_UP:
                if (pointerCount == 2 && isTwoFingerState()) {
                    updateTwoFinger(x, y);
                    pendingTwoFingerTap = state == State.TWO_UNDECIDED
                            && isWithinTapTime(eventTime)
                            && twoMaximumTravel <= tapMovementSlop;
                }
                else {
                    pendingTwoFingerTap = false;
                }
                state = State.WAIT_FOR_ALL_UP;
                return true;

            case ACTION_UP:
                finishGesture(x[0], y[0], eventTime);
                return true;

            case ACTION_CANCEL:
                cancel();
                return true;

            default:
                return false;
        }
    }

    void cancel() {
        state = State.IDLE;
        pendingTwoFingerTap = false;
        singleMaximumTravel = 0f;
        twoMaximumTravel = 0f;
    }

    private void beginSingle(float x, float y, long eventTime) {
        cancel();
        state = State.SINGLE;
        gestureDownTime = eventTime;
        singleStartX = singleLastX = x;
        singleStartY = singleLastY = y;
    }

    private void updateSingle(float x, float y) {
        singleMaximumTravel = Math.max(singleMaximumTravel,
                distance(singleStartX, singleStartY, x, y));
        float deltaX = x - singleLastX;
        float deltaY = y - singleLastY;
        singleLastX = x;
        singleLastY = y;
        if (deltaX != 0f || deltaY != 0f) {
            listener.onPointerMove(deltaX, deltaY);
        }
    }

    private void beginTwoFinger(float[] x, float[] y) {
        state = State.TWO_UNDECIDED;
        pendingTwoFingerTap = false;
        twoStartX[0] = x[0];
        twoStartX[1] = x[1];
        twoStartY[0] = y[0];
        twoStartY[1] = y[1];
        twoMaximumTravel = 0f;
        twoInitialSpan = twoLastSpan = distance(x[0], y[0], x[1], y[1]);
        twoInitialFocusX = (x[0] + x[1]) * 0.5f;
        twoInitialFocusY = twoLastFocusY = (y[0] + y[1]) * 0.5f;
    }

    private void updateTwoFinger(float[] x, float[] y) {
        twoMaximumTravel = Math.max(twoMaximumTravel,
                Math.max(distance(twoStartX[0], twoStartY[0], x[0], y[0]),
                        distance(twoStartX[1], twoStartY[1], x[1], y[1])));

        float span = distance(x[0], y[0], x[1], y[1]);
        float focusX = (x[0] + x[1]) * 0.5f;
        float focusY = (y[0] + y[1]) * 0.5f;

        if (state == State.TWO_UNDECIDED) {
            float spanDelta = Math.abs(span - twoInitialSpan);
            float spanRatio = spanDelta / Math.max(twoInitialSpan, 1f);
            float focusDeltaX = focusX - twoInitialFocusX;
            float focusDeltaY = focusY - twoInitialFocusY;

            // Pinch takes precedence once both absolute and relative evidence are present.
            if (spanDelta >= pinchActivationDistance && spanRatio >= pinchActivationRatio) {
                state = State.TWO_PINCH;
                dispatchZoom(twoInitialSpan, span, focusX, focusY);
            }
            else if (Math.abs(focusDeltaY) >= scrollActivationDistance
                    && Math.abs(focusDeltaY) >= Math.abs(focusDeltaX)) {
                state = State.TWO_SCROLL;
                listener.onVerticalScroll(focusDeltaY);
            }
        }
        else if (state == State.TWO_PINCH) {
            dispatchZoom(twoLastSpan, span, focusX, focusY);
        }
        else if (state == State.TWO_SCROLL) {
            float deltaY = focusY - twoLastFocusY;
            if (deltaY != 0f) {
                listener.onVerticalScroll(deltaY);
            }
        }

        twoLastSpan = span;
        twoLastFocusY = focusY;
    }

    private void dispatchZoom(float previousSpan, float currentSpan, float focusX, float focusY) {
        if (previousSpan <= 0f || currentSpan <= 0f) {
            return;
        }
        float scaleFactor = currentSpan / previousSpan;
        if (!Float.isNaN(scaleFactor) && !Float.isInfinite(scaleFactor) && scaleFactor > 0f) {
            listener.onZoom(scaleFactor, focusX, focusY);
        }
    }

    private void finishGesture(float x, float y, long eventTime) {
        if (state == State.SINGLE) {
            singleMaximumTravel = Math.max(singleMaximumTravel,
                    distance(singleStartX, singleStartY, x, y));
            if (isWithinTapTime(eventTime) && singleMaximumTravel <= tapMovementSlop) {
                listener.onLeftClick();
            }
        }
        else if (state == State.WAIT_FOR_ALL_UP && pendingTwoFingerTap) {
            listener.onRightClick();
        }
        cancel();
    }

    private boolean isWithinTapTime(long eventTime) {
        return eventTime - gestureDownTime <= tapTimeoutMs;
    }

    private boolean isTwoFingerState() {
        return state == State.TWO_UNDECIDED
                || state == State.TWO_SCROLL
                || state == State.TWO_PINCH;
    }

    private static float distance(float x1, float y1, float x2, float y2) {
        return (float) Math.hypot(x2 - x1, y2 - y1);
    }
}
