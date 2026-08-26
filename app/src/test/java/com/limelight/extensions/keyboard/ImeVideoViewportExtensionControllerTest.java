package com.limelight.extensions.keyboard;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * EXTENSION DEVELOPMENT [EXT-IME-VIDEO-VIEWPORT] [ADDED]
 * Coverage for host-space viewport inset calculations.
 */
public class ImeVideoViewportExtensionControllerTest {
    @Test
    public void resizedWindowReservesOnlyAccessoryBarArea() {
        assertEquals(84, ImeVideoViewportExtensionController
                .calculateReservedBottomInsetPx(500, 416));
    }

    @Test
    public void fullscreenWindowReservesImeAndAccessoryBarArea() {
        assertEquals(484, ImeVideoViewportExtensionController
                .calculateReservedBottomInsetPx(900, 416));
    }

    @Test
    public void accessoryBarTopIsClampedToHostBounds() {
        assertEquals(500, ImeVideoViewportExtensionController
                .calculateReservedBottomInsetPx(500, -20));
        assertEquals(0, ImeVideoViewportExtensionController
                .calculateReservedBottomInsetPx(500, 700));
        assertEquals(0, ImeVideoViewportExtensionController
                .calculateReservedBottomInsetPx(-1, 10));
    }
}
