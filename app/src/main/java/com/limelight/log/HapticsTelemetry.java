package com.limelight.log;

import android.os.SystemClock;

import java.util.Locale;

/** Coalesces high-frequency rumble callbacks into low-volume session diagnostics. */
public final class HapticsTelemetry {
    private static final int MAX_CONTROLLERS = 16;

    private final StreamSessionLogger logger;
    private final int[] lastLow = new int[MAX_CONTROLLERS];
    private final int[] lastHigh = new int[MAX_CONTROLLERS];
    private final long[] activeStartedMs = new long[MAX_CONTROLLERS];
    private final long[] activeCommands = new long[MAX_CONTROLLERS];
    private final String[] lastRoute = new String[MAX_CONTROLLERS];

    private long windowCommands;
    private long windowNonZeroCommands;
    private long windowZeroCommands;
    private long windowIgnoredCommands;
    private int windowMaxLow;
    private int windowMaxHigh;

    public HapticsTelemetry(StreamSessionLogger logger) {
        this.logger = logger;
    }

    public synchronized void recordRumble(short controllerNumber, short lowFreqMotor,
                                          short highFreqMotor, String route,
                                          boolean ignoredWhileBackgrounded) {
        int controller = controllerNumber & 0xFFFF;
        int low = lowFreqMotor & 0xFFFF;
        int high = highFreqMotor & 0xFFFF;
        windowCommands++;
        if (ignoredWhileBackgrounded) {
            windowIgnoredCommands++;
            return;
        }

        boolean active = low != 0 || high != 0;
        if (active) {
            windowNonZeroCommands++;
            windowMaxLow = Math.max(windowMaxLow, low);
            windowMaxHigh = Math.max(windowMaxHigh, high);
        }
        else {
            windowZeroCommands++;
        }

        if (controller >= MAX_CONTROLLERS) {
            return;
        }
        boolean wasActive = lastLow[controller] != 0 || lastHigh[controller] != 0;
        String safeRoute = empty(route) ? "none" : safe(route);
        if (active) {
            activeCommands[controller]++;
            if (!empty(route)) {
                lastRoute[controller] = safeRoute;
            }
            if (!wasActive) {
                activeStartedMs[controller] = SystemClock.elapsedRealtime();
                logger.important("RUMBLE_START", String.format(Locale.US,
                        "pad=%d, low=%d, high=%d, route=%s",
                        controller, low, high, safeRoute));
            }
        }
        else if (wasActive) {
            long durationMs = activeStartedMs[controller] == 0L ? 0L
                    : Math.max(0L, SystemClock.elapsedRealtime() - activeStartedMs[controller]);
            logger.important("RUMBLE_STOP", String.format(Locale.US,
                    "pad=%d, source=host-zero, durationMs=%d, commands=%d, last=%d/%d, route=%s",
                    controller, durationMs, activeCommands[controller],
                    lastLow[controller], lastHigh[controller],
                    empty(lastRoute[controller]) ? safeRoute : lastRoute[controller]));
            activeStartedMs[controller] = 0L;
            activeCommands[controller] = 0L;
        }
        lastLow[controller] = low;
        lastHigh[controller] = high;
    }

    public synchronized void recordExplicitStop(String reason, String route) {
        int activeControllers = 0;
        StringBuilder states = new StringBuilder();
        for (int controller = 0; controller < MAX_CONTROLLERS; controller++) {
            if (lastLow[controller] == 0 && lastHigh[controller] == 0) {
                continue;
            }
            activeControllers++;
            if (states.length() > 0) {
                states.append(';');
            }
            states.append(controller).append(':').append(lastLow[controller])
                    .append('/').append(lastHigh[controller]);
            lastLow[controller] = 0;
            lastHigh[controller] = 0;
            activeStartedMs[controller] = 0L;
            activeCommands[controller] = 0L;
        }
        logger.important("HAPTICS_STOP", "reason=" + safe(reason)
                + ", activePads=" + activeControllers
                + ", previous=" + (states.length() == 0 ? "none" : states)
                + ", route=" + (empty(route) ? "none" : safe(route))
                + ", explicitZeroRequested=true");
    }

    public synchronized String snapshotAndReset() {
        if (windowCommands == 0L) {
            return "";
        }
        int activeControllers = 0;
        StringBuilder last = new StringBuilder();
        for (int controller = 0; controller < MAX_CONTROLLERS; controller++) {
            if (lastLow[controller] == 0 && lastHigh[controller] == 0) {
                continue;
            }
            activeControllers++;
            if (last.length() > 0) {
                last.append(';');
            }
            last.append(controller).append(':').append(lastLow[controller])
                    .append('/').append(lastHigh[controller]);
        }
        String result = "commands=" + windowCommands
                + ", nonZero=" + windowNonZeroCommands
                + ", zero=" + windowZeroCommands
                + ", ignoredBackground=" + windowIgnoredCommands
                + ", max=" + windowMaxLow + "/" + windowMaxHigh
                + ", activePads=" + activeControllers
                + ", last=" + (last.length() == 0 ? "none" : last);
        windowCommands = 0L;
        windowNonZeroCommands = 0L;
        windowZeroCommands = 0L;
        windowIgnoredCommands = 0L;
        windowMaxLow = 0;
        windowMaxHigh = 0;
        return result;
    }

    private static boolean empty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String safe(String value) {
        return empty(value) ? "unknown" : value.replace(',', '_')
                .replace('\r', ' ').replace('\n', ' ');
    }
}
