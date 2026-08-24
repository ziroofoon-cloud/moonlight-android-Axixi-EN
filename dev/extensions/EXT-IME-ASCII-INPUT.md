# EXT-IME-ASCII-INPUT

- Change type: compatibility extension development
- Status: experimental
- Added: 2026-08-24
- Purpose: avoid Sunshine/Linux `Ctrl+Shift+U` Unicode composition for Android IME commits
  containing printable ASCII by sending normalized keyboard packets instead.
- Added files:
  - `app/src/main/java/com/limelight/extensions/input/ImeAsciiInputExtension.java`
  - `app/src/test/java/com/limelight/extensions/input/ImeAsciiInputExtensionTest.java`
- Modified files:
  - `app/src/main/java/com/limelight/Game.java`
- Upstream isolation: the extension has no dependency on the IME accessory bar or video viewport;
  `Game` only routes committed text and printable virtual-keyboard events through it, then sends the
  returned normalized strokes.
- Atomic fallback: a complete commit is mapped and translated before the first key packet is sent,
  preventing a partially sent commit from being duplicated by UTF-8 fallback.
- Virtual-key compatibility: printable events from Android's virtual keyboard are routed before the
  original raw-key translation. This preserves required Shift state for dedicated events such as
  `KEYCODE_PLUS`, which otherwise translates to the unshifted `=` base key.
- Compatibility constraint: printable ASCII mappings assume a US/QWERTY host keyboard layout.
  Empty commits, control characters, non-ASCII text, and mixed unsupported commits remain on the
  original UTF-8 path.
- Related upstream issue: `LizardByte/Sunshine#5274`.
