# EXT-IME-ACCESSORY-BAR

- Change type: extension development
- Status: experimental
- Added: 2026-08-24
- Purpose: display two rows of desktop shortcut keys directly above a docked Android system IME.
- Key rows:
  - `ESC, TAB, Unknown, HOME, ↑, END, PGUP`
  - `CMD, CTRL, ALT, ←, ↓, →, PGDN`
- Added files:
  - `app/src/main/java/com/limelight/extensions/keyboard/ImeKeyboardExtensionController.java`
  - `app/src/main/java/com/limelight/extensions/keyboard/ImeVideoViewportExtensionController.java`
  - `app/src/main/res/layout/extension_ime_keyboard_bar.xml`
  - `app/src/main/res/drawable/extension_ime_keyboard_key.xml`
  - `app/src/main/res/values/extension_ime_keyboard_styles.xml`
  - `app/src/test/java/com/limelight/extensions/keyboard/ImeKeyboardExtensionControllerTest.java`
  - `app/src/test/java/com/limelight/extensions/keyboard/ImeVideoViewportExtensionControllerTest.java`
- Modified files:
  - `app/src/main/java/com/limelight/Game.java`
- Upstream isolation: the controllers own their views, resources, IME observation, key state,
  inset calculation, and temporary video-view layout changes. `Game` only attaches the extension,
  forwards keys through the existing keyboard pipeline, and supplies a video-layout callback.
- Modifier behavior: `CMD`, `CTRL`, and `ALT` latch until tapped again. `CMD` emits the standard
  Meta key, which is Win on Linux/Windows hosts and Command on macOS hosts.
- Viewport behavior: while the portrait accessory is visible, the local remote-video view ends at
  the accessory's top edge. Stream resolution and decoder surface buffer size are unchanged, and
  original view layout values are restored when the IME is hidden or the extension is destroyed.
- Presentation constraint: the accessory bar and viewport reservation are disabled in landscape;
  floating or split IMEs do not show the docked accessory.
