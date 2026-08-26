package com.limelight.extensions.input;

import android.view.KeyEvent;

import com.limelight.nvstream.input.KeyboardPacket;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * EXTENSION DEVELOPMENT [EXT-IME-ASCII-INPUT] [ADDED]
 * Coverage for complete printable-ASCII IME commit mapping.
 */
public class ImeAsciiInputExtensionTest {
    @Test
    public void mapsEveryPrintableAsciiCharacter() {
        StringBuilder printableAscii = new StringBuilder();
        for (char character = 0x20; character <= 0x7e; character++) {
            printableAscii.append(character);
        }

        ImeAsciiInputExtension.KeyStroke[] keyStrokes =
                ImeAsciiInputExtension.mapText(printableAscii.toString());

        assertNotNull(keyStrokes);
        assertEquals(95, keyStrokes.length);
        for (ImeAsciiInputExtension.KeyStroke keyStroke : keyStrokes) {
            assertNotNull(keyStroke);
        }
    }

    @Test
    public void mapsAtSignAsShiftTwo() {
        assertSingleMapping("@", KeyEvent.KEYCODE_2, KeyboardPacket.MODIFIER_SHIFT);
    }

    @Test
    public void mapsLetterCaseAndPunctuationModifiers() {
        assertSingleMapping("a", KeyEvent.KEYCODE_A, (byte) 0);
        assertSingleMapping("A", KeyEvent.KEYCODE_A, KeyboardPacket.MODIFIER_SHIFT);
        assertSingleMapping("/", KeyEvent.KEYCODE_SLASH, (byte) 0);
        assertSingleMapping("?", KeyEvent.KEYCODE_SLASH, KeyboardPacket.MODIFIER_SHIFT);
        assertSingleMapping("=", KeyEvent.KEYCODE_EQUALS, (byte) 0);
        assertSingleMapping("+", KeyEvent.KEYCODE_EQUALS, KeyboardPacket.MODIFIER_SHIFT);
        assertSingleMapping("[", KeyEvent.KEYCODE_LEFT_BRACKET, (byte) 0);
        assertSingleMapping("{", KeyEvent.KEYCODE_LEFT_BRACKET, KeyboardPacket.MODIFIER_SHIFT);
    }

    @Test
    public void mapsEveryShiftedUsQwertySymbolToItsBaseKey() {
        String shiftedSymbols = "!\"#$%&()*+:<>?@^_{|}~";
        int[] expectedKeyCodes = {
                KeyEvent.KEYCODE_1,
                KeyEvent.KEYCODE_APOSTROPHE,
                KeyEvent.KEYCODE_3,
                KeyEvent.KEYCODE_4,
                KeyEvent.KEYCODE_5,
                KeyEvent.KEYCODE_7,
                KeyEvent.KEYCODE_9,
                KeyEvent.KEYCODE_0,
                KeyEvent.KEYCODE_8,
                KeyEvent.KEYCODE_EQUALS,
                KeyEvent.KEYCODE_SEMICOLON,
                KeyEvent.KEYCODE_COMMA,
                KeyEvent.KEYCODE_PERIOD,
                KeyEvent.KEYCODE_SLASH,
                KeyEvent.KEYCODE_2,
                KeyEvent.KEYCODE_6,
                KeyEvent.KEYCODE_MINUS,
                KeyEvent.KEYCODE_LEFT_BRACKET,
                KeyEvent.KEYCODE_BACKSLASH,
                KeyEvent.KEYCODE_RIGHT_BRACKET,
                KeyEvent.KEYCODE_GRAVE,
        };

        ImeAsciiInputExtension.KeyStroke[] keyStrokes =
                ImeAsciiInputExtension.mapText(shiftedSymbols);

        assertNotNull(keyStrokes);
        assertEquals(expectedKeyCodes.length, keyStrokes.length);
        for (int i = 0; i < expectedKeyCodes.length; i++) {
            assertEquals(expectedKeyCodes[i], keyStrokes[i].getAndroidKeyCode());
            assertEquals(KeyboardPacket.MODIFIER_SHIFT,
                    keyStrokes[i].getRequiredModifiers());
        }
    }

    @Test
    public void recognizesOnlySinglePrintableAsciiCodePoints() {
        assertTrue(ImeAsciiInputExtension.isPrintableAscii(0x20));
        assertTrue(ImeAsciiInputExtension.isPrintableAscii(0x7e));
        assertFalse(ImeAsciiInputExtension.isPrintableAscii(0x1f));
        assertFalse(ImeAsciiInputExtension.isPrintableAscii(0x7f));
        assertFalse(ImeAsciiInputExtension.isPrintableAscii('中'));
    }

    @Test
    public void mapsCompleteMultiCharacterCommitInOrder() {
        ImeAsciiInputExtension.KeyStroke[] keyStrokes =
                ImeAsciiInputExtension.mapText("name@example.com");

        assertNotNull(keyStrokes);
        assertEquals(16, keyStrokes.length);
        assertEquals(KeyEvent.KEYCODE_N, keyStrokes[0].getAndroidKeyCode());
        assertEquals(KeyEvent.KEYCODE_2, keyStrokes[4].getAndroidKeyCode());
        assertEquals(KeyboardPacket.MODIFIER_SHIFT, keyStrokes[4].getRequiredModifiers());
        assertEquals(KeyEvent.KEYCODE_PERIOD, keyStrokes[12].getAndroidKeyCode());
        assertEquals(KeyEvent.KEYCODE_M, keyStrokes[15].getAndroidKeyCode());
    }

    @Test
    public void rejectsIncompleteOrNonAsciiCommitWithoutPartialMapping() {
        assertNull(ImeAsciiInputExtension.mapText(null));
        assertNull(ImeAsciiInputExtension.mapText(""));
        assertNull(ImeAsciiInputExtension.mapText("中文"));
        assertNull(ImeAsciiInputExtension.mapText("abc中"));
        assertNull(ImeAsciiInputExtension.mapText("line\nbreak"));
    }

    private static void assertSingleMapping(String text, int keyCode, byte modifiers) {
        ImeAsciiInputExtension.KeyStroke[] keyStrokes =
                ImeAsciiInputExtension.mapText(text);

        assertNotNull(keyStrokes);
        assertEquals(1, keyStrokes.length);
        assertEquals(keyCode, keyStrokes[0].getAndroidKeyCode());
        assertEquals(modifiers, keyStrokes[0].getRequiredModifiers());
    }
}
