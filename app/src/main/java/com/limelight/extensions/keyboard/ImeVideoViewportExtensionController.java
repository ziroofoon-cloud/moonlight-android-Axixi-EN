package com.limelight.extensions.keyboard;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

/**
 * EXTENSION DEVELOPMENT [EXT-IME-VIDEO-VIEWPORT] [ADDED]
 *
 * <p>Reserves the area below the IME accessory bar for the system IME and the bar itself. The
 * remote video remains a local rendering concern: stream resolution and decoder surface buffer
 * size are not changed.</p>
 *
 * <p>The controller temporarily updates only the bottom margin and vertical gravity of the video
 * views. Original values are restored when the IME is hidden or the extension is destroyed.</p>
 */
public final class ImeVideoViewportExtensionController {
    private static final int UNSET_INSET = -1;

    private final VideoViewLayoutState systemVideoViewState;
    private final VideoViewLayoutState processedVideoViewState;
    private final View layoutRefreshAnchor;
    private final Runnable layoutChangedCallback;
    private final View.OnLayoutChangeListener layoutChangeListener;

    private boolean destroyed;
    private int appliedBottomInsetPx = UNSET_INSET;

    public ImeVideoViewportExtensionController(View systemVideoView,
                                               View processedVideoView,
                                               Runnable layoutChangedCallback) {
        if (systemVideoView == null) {
            throw new IllegalArgumentException("systemVideoView must not be null");
        }

        this.systemVideoViewState = new VideoViewLayoutState(systemVideoView);
        this.processedVideoViewState = processedVideoView != null
                && processedVideoView != systemVideoView
                ? new VideoViewLayoutState(processedVideoView)
                : null;
        this.layoutRefreshAnchor = processedVideoView != null
                ? processedVideoView
                : systemVideoView;
        this.layoutChangedCallback = layoutChangedCallback;
        this.layoutChangeListener = (view, left, top, right, bottom,
                                     oldLeft, oldTop, oldRight, oldBottom) -> {
            if (!destroyed && layoutChangedCallback != null
                    && (left != oldLeft || top != oldTop
                    || right != oldRight || bottom != oldBottom)) {
                layoutChangedCallback.run();
            }
        };
        this.layoutRefreshAnchor.addOnLayoutChangeListener(layoutChangeListener);
    }

    /** Returns the host-space area from the accessory bar top through the host bottom. */
    static int calculateReservedBottomInsetPx(int hostHeightPx, int accessoryBarTopPx) {
        int safeHostHeight = Math.max(0, hostHeightPx);
        int safeBarTop = Math.max(0, Math.min(accessoryBarTopPx, safeHostHeight));
        return safeHostHeight - safeBarTop;
    }

    public void setReservedBottomInsetPx(int requestedBottomInsetPx) {
        if (destroyed) {
            return;
        }

        int bottomInsetPx = clampToParentHeight(Math.max(0, requestedBottomInsetPx));
        if (appliedBottomInsetPx == bottomInsetPx) {
            return;
        }
        appliedBottomInsetPx = bottomInsetPx;

        systemVideoViewState.applyBottomInset(bottomInsetPx);
        if (processedVideoViewState != null) {
            processedVideoViewState.applyBottomInset(bottomInsetPx);
        }
    }

    public void destroy() {
        if (destroyed) {
            return;
        }
        destroyed = true;
        layoutRefreshAnchor.removeOnLayoutChangeListener(layoutChangeListener);
        systemVideoViewState.restore();
        if (processedVideoViewState != null) {
            processedVideoViewState.restore();
        }
    }

    private int clampToParentHeight(int bottomInsetPx) {
        ViewParent parent = systemVideoViewState.view.getParent();
        if (!(parent instanceof View)) {
            return bottomInsetPx;
        }

        int parentHeight = ((View) parent).getHeight();
        if (parentHeight <= 1) {
            return bottomInsetPx;
        }
        return Math.min(bottomInsetPx, parentHeight - 1);
    }

    private static final class VideoViewLayoutState {
        private final View view;
        private final int originalBottomMargin;
        private final int originalGravity;
        private final boolean supportedLayoutParams;

        VideoViewLayoutState(View view) {
            this.view = view;
            ViewGroup.LayoutParams rawLayoutParams = view.getLayoutParams();
            if (rawLayoutParams instanceof FrameLayout.LayoutParams) {
                FrameLayout.LayoutParams layoutParams =
                        (FrameLayout.LayoutParams) rawLayoutParams;
                originalBottomMargin = layoutParams.bottomMargin;
                originalGravity = layoutParams.gravity;
                supportedLayoutParams = true;
            }
            else {
                originalBottomMargin = 0;
                originalGravity = Gravity.NO_GRAVITY;
                supportedLayoutParams = false;
            }
        }

        void applyBottomInset(int bottomInsetPx) {
            if (!supportedLayoutParams) {
                return;
            }

            FrameLayout.LayoutParams layoutParams =
                    (FrameLayout.LayoutParams) view.getLayoutParams();
            int desiredBottomMargin = originalBottomMargin + bottomInsetPx;
            int desiredGravity = bottomInsetPx == 0
                    ? originalGravity
                    : withBottomGravity(originalGravity);
            if (layoutParams.bottomMargin == desiredBottomMargin
                    && layoutParams.gravity == desiredGravity) {
                return;
            }

            layoutParams.bottomMargin = desiredBottomMargin;
            layoutParams.gravity = desiredGravity;
            view.setLayoutParams(layoutParams);
        }

        void restore() {
            applyBottomInset(0);
        }

        private static int withBottomGravity(int gravity) {
            int resolvedGravity = gravity;
            if (resolvedGravity == -1 || resolvedGravity == Gravity.NO_GRAVITY) {
                resolvedGravity = Gravity.CENTER;
            }
            return (resolvedGravity & ~Gravity.VERTICAL_GRAVITY_MASK) | Gravity.BOTTOM;
        }
    }
}
