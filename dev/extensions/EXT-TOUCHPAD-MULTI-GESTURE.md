# EXT-TOUCHPAD-MULTI-GESTURE

- Change type: extension development
- Status: experimental
- Added: 2026-08-24
- Purpose: provide an isolated `触控板「多手势」` mouse/touch mode with single-finger movement
  and tap, two-finger tap and vertical scroll, pinch zoom, and cursor-following edge pan.
- Added files:
  - `app/src/main/java/com/limelight/extensions/input/touch/MultiGestureCursorTracker.java`
  - `app/src/main/java/com/limelight/extensions/input/touch/MultiGestureEdgePanCalculator.java`
  - `app/src/main/java/com/limelight/extensions/input/touch/MultiGestureTouchContext.java`
  - `app/src/main/java/com/limelight/extensions/input/touch/MultiGestureTouchpadExtensionController.java`
  - `app/src/main/java/com/limelight/extensions/input/touch/MultiGestureViewportCalculator.java`
- Added test files:
  - `app/src/test/java/com/limelight/extensions/input/touch/MultiGestureCursorTrackerTest.java`
  - `app/src/test/java/com/limelight/extensions/input/touch/MultiGestureEdgePanCalculatorTest.java`
  - `app/src/test/java/com/limelight/extensions/input/touch/MultiGestureTouchContextTest.java`
  - `app/src/test/java/com/limelight/extensions/input/touch/MultiGestureViewportCalculatorTest.java`
- Modified files:
  - `app/src/main/java/com/limelight/Game.java`
  - `app/src/main/java/com/limelight/ui/gamemenu/GameMenuFragment.java`
  - `app/src/main/java/com/limelight/ui/video/VideoZoomController.java`
  - `app/src/main/res/values/arrays.xml`
- Gesture classification: each two-finger gesture starts undecided, then locks to vertical scroll or
  pinch zoom after crossing its threshold. The classification remains locked until every finger is
  lifted, preventing zoom/scroll crossover and one-finger jumps after a two-finger action.
- Input mapping: one-finger motion updates the mode-owned absolute cursor; one-finger tap sends
  left-click; two-finger tap sends right-click regardless of finger release order; vertical motion
  sends host scroll; pinch and spread update the existing session-local video transform.
- Upstream isolation: the new mode has its own stable value (`7`) and a single public Android
  controller. Its cursor tracker, gesture state machine, and geometry calculators remain
  package-private. Existing mode values and touch-context implementations are unchanged.
- Merge-order isolation: this extension has no Java or resource dependency on the IME accessory,
  ASCII input, or IME viewport extensions. It reads an optional bottom layout margin through the
  existing video view, so the IME viewport extension can be merged before, after, or not at all.
- Overlay compatibility: the standalone zoom and virtual-mouse touch layers are suppressed while
  this mode owns touchscreen gestures, without resetting the current video transform.
- Edge following: at zoom levels above 1x, the extension pans only when its tracked cursor reaches
  an edge with transformed video content hidden beyond it. The trigger aperture is the actual
  gesture viewport; an optional bottom layout reservation moves only the bottom trigger edge.
- Absolute-cursor isolation: this mode owns a 1280x720 logical cursor plane independently of the
  global remote-desktop mouse setting. This is packet reference geometry, not stream resolution.
  The first touchscreen contact synchronizes the host cursor to the logical center, and subsequent
  one-finger movement sends absolute positions from the tracker also used by edge following.
- Edge-follow limitation: the streaming protocol does not return the current remote cursor position
  to this Java input path. Host-side cursor warps or simultaneous physical-mouse input can desync the
  tracked position; selecting the mode again resets the tracker to the video center.
- Menu compatibility: appended game-menu actions use the resource-defined mode count instead of
  fixed indexes, so adding this mode does not redirect those actions.
