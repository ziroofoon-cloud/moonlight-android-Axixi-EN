package com.limelight.extensions.input;

import android.view.KeyEvent;

import com.limelight.nvstream.input.KeyboardPacket;

/**
 * EXTENSION DEVELOPMENT [EXT-IME-ASCII-INPUT] [ADDED]
 *
 * <p>Maps printable ASCII text committed by an Android IME to normalized keyboard strokes.</p>
 *
 * <p>Sunshine on Linux may inject UTF-8 text through a Ctrl+Shift+U Unicode input sequence.
 * Applications that do not support that sequence can display an unfinished {@code U+}
 * composition. This extension avoids that path for printable ASCII while leaving all other text
 * on Moonlight's original UTF-8 input path.</p>
 *
 * <p>These mappings assume a US/QWERTY host keyboard layout. The complete input is mapped before
 * any key is sent, allowing the integration point to fall back without partially entering text.</p>
 */
public final class ImeAsciiInputExtension {
    public static final class KeyStroke {
        private final int androidKeyCode;
        private final byte requiredModifiers;

        private KeyStroke(int androidKeyCode, byte requiredModifiers) {
            this.androidKeyCode = androidKeyCode;
            this.requiredModifiers = requiredModifiers;
        }

        public int getAndroidKeyCode() {
            return androidKeyCode;
        }

        public byte getRequiredModifiers() {
            return requiredModifiers;
        }
    }

    private ImeAsciiInputExtension() {
    }

    /**
     * Maps a complete text commit to printable US/QWERTY ASCII key strokes.
     *
     * @return all mapped strokes, or {@code null} when the complete input must use the existing
     *         UTF-8 path
     */
    public static KeyStroke[] mapText(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        KeyStroke[] keyStrokes = new KeyStroke[text.length()];
        for (int i = 0; i < text.length(); i++) {
            KeyStroke keyStroke = mapCharacter(text.charAt(i));
            if (keyStroke == null) {
                return null;
            }
            keyStrokes[i] = keyStroke;
        }
        return keyStrokes;
    }

    /**
     * Returns whether a Unicode value can be handled by this extension as a single character.
     */
    public static boolean isPrintableAscii(int unicodeCharacter) {
        return unicodeCharacter >= 0x20 && unicodeCharacter <= 0x7e;
    }

    private static KeyStroke mapCharacter(char character) {
        if (character >= '0' && character <= '9') {
            return unshifted(KeyEvent.KEYCODE_0 + character - '0');
        }
        if (character >= 'a' && character <= 'z') {
            return unshifted(KeyEvent.KEYCODE_A + character - 'a');
        }
        if (character >= 'A' && character <= 'Z') {
            return shifted(KeyEvent.KEYCODE_A + character - 'A');
        }

        switch (character) {
            case ' ':
                return unshifted(KeyEvent.KEYCODE_SPACE);
            case '!':
                return shifted(KeyEvent.KEYCODE_1);
            case '"':
                return shifted(KeyEvent.KEYCODE_APOSTROPHE);
            case '#':
                return shifted(KeyEvent.KEYCODE_3);
            case '$':
                return shifted(KeyEvent.KEYCODE_4);
            case '%':
                return shifted(KeyEvent.KEYCODE_5);
            case '&':
                return shifted(KeyEvent.KEYCODE_7);
            case '\'':
                return unshifted(KeyEvent.KEYCODE_APOSTROPHE);
            case '(':
                return shifted(KeyEvent.KEYCODE_9);
            case ')':
                return shifted(KeyEvent.KEYCODE_0);
            case '*':
                return shifted(KeyEvent.KEYCODE_8);
            case '+':
                return shifted(KeyEvent.KEYCODE_EQUALS);
            case ',':
                return unshifted(KeyEvent.KEYCODE_COMMA);
            case '-':
                return unshifted(KeyEvent.KEYCODE_MINUS);
            case '.':
                return unshifted(KeyEvent.KEYCODE_PERIOD);
            case '/':
                return unshifted(KeyEvent.KEYCODE_SLASH);
            case ':':
                return shifted(KeyEvent.KEYCODE_SEMICOLON);
            case ';':
                return unshifted(KeyEvent.KEYCODE_SEMICOLON);
            case '<':
                return shifted(KeyEvent.KEYCODE_COMMA);
            case '=':
                return unshifted(KeyEvent.KEYCODE_EQUALS);
            case '>':
                return shifted(KeyEvent.KEYCODE_PERIOD);
            case '?':
                return shifted(KeyEvent.KEYCODE_SLASH);
            case '@':
                return shifted(KeyEvent.KEYCODE_2);
            case '[':
                return unshifted(KeyEvent.KEYCODE_LEFT_BRACKET);
            case '\\':
                return unshifted(KeyEvent.KEYCODE_BACKSLASH);
            case ']':
                return unshifted(KeyEvent.KEYCODE_RIGHT_BRACKET);
            case '^':
                return shifted(KeyEvent.KEYCODE_6);
            case '_':
                return shifted(KeyEvent.KEYCODE_MINUS);
            case '`':
                return unshifted(KeyEvent.KEYCODE_GRAVE);
            case '{':
                return shifted(KeyEvent.KEYCODE_LEFT_BRACKET);
            case '|':
                return shifted(KeyEvent.KEYCODE_BACKSLASH);
            case '}':
                return shifted(KeyEvent.KEYCODE_RIGHT_BRACKET);
            case '~':
                return shifted(KeyEvent.KEYCODE_GRAVE);
            default:
                return null;
        }
    }

    private static KeyStroke unshifted(int androidKeyCode) {
        return new KeyStroke(androidKeyCode, (byte) 0);
    }

    private static KeyStroke shifted(int androidKeyCode) {
        return new KeyStroke(androidKeyCode, KeyboardPacket.MODIFIER_SHIFT);
    }
}
