package com.limelight.extensions.keyboard;

import android.content.res.Configuration;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * EXTENSION DEVELOPMENT [EXT-IME-ACCESSORY-BAR] [ADDED]
 * Coverage for orientation-based accessory availability.
 */
public class ImeKeyboardExtensionControllerTest {
    @Test
    public void landscapeDisablesAccessoryBarAndViewportReservation() {
        assertFalse(ImeKeyboardExtensionController.isAccessoryEnabledForOrientation(
                Configuration.ORIENTATION_LANDSCAPE));
    }

    @Test
    public void portraitEnablesAccessoryBar() {
        assertTrue(ImeKeyboardExtensionController.isAccessoryEnabledForOrientation(
                Configuration.ORIENTATION_PORTRAIT));
    }

    @Test
    public void undefinedOrientationDoesNotDisableAccessoryBar() {
        assertTrue(ImeKeyboardExtensionController.isAccessoryEnabledForOrientation(
                Configuration.ORIENTATION_UNDEFINED));
    }
}
