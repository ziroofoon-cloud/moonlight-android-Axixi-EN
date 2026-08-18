package com.limelight.log;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Low-volume, privacy-safe network link change monitor for a stream session. */
public final class StreamNetworkMonitor {
    public interface Callback {
        void onNetworkChanged(String summary);
    }

    private final ConnectivityManager connectivityManager;
    private final Callback callback;
    private final ConnectivityManager.NetworkCallback networkCallback;
    private boolean registered;
    private String lastSummary = "";

    public StreamNetworkMonitor(Context context, Callback callback) {
        connectivityManager = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        this.callback = callback;
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                emitIfChanged();
            }

            @Override
            public void onLost(Network network) {
                emitIfChanged();
            }

            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities capabilities) {
                emitIfChanged();
            }

            @Override
            public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
                emitIfChanged();
            }
        };
    }

    public void start() {
        emitIfChanged();
        if (connectivityManager == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return;
        }
        try {
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
            registered = true;
        }
        catch (RuntimeException ignored) {
            // Link telemetry is diagnostic-only and must not affect stream startup.
        }
    }

    public void stop() {
        if (!registered || connectivityManager == null) {
            return;
        }
        registered = false;
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
        catch (RuntimeException ignored) {
        }
    }

    public synchronized String getCurrentSummary() {
        return buildSummary();
    }

    private void emitIfChanged() {
        String summary;
        synchronized (this) {
            summary = buildSummary();
            if (summary.equals(lastSummary)) {
                return;
            }
            lastSummary = summary;
        }
        if (callback != null) {
            callback.onNetworkChanged(summary);
        }
    }

    private String buildSummary() {
        if (connectivityManager == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return "transport=UNKNOWN, capabilities=unavailable";
        }
        Network network = connectivityManager.getActiveNetwork();
        if (network == null) {
            return "transport=NONE";
        }
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        if (capabilities == null) {
            return "transport=UNKNOWN, capabilities=missing";
        }

        List<String> transports = new ArrayList<>();
        addTransport(capabilities, transports, NetworkCapabilities.TRANSPORT_WIFI, "WIFI");
        addTransport(capabilities, transports, NetworkCapabilities.TRANSPORT_ETHERNET, "ETHERNET");
        addTransport(capabilities, transports, NetworkCapabilities.TRANSPORT_CELLULAR, "CELLULAR");
        addTransport(capabilities, transports, NetworkCapabilities.TRANSPORT_VPN, "VPN");
        addTransport(capabilities, transports, NetworkCapabilities.TRANSPORT_BLUETOOTH, "BLUETOOTH");
        if (transports.isEmpty()) {
            transports.add("OTHER");
        }

        StringBuilder builder = new StringBuilder(160);
        builder.append("transport=").append(join(transports));
        builder.append(", metered=").append(connectivityManager.isActiveNetworkMetered());
        builder.append(", validated=").append(capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_VALIDATED));
        builder.append(", internet=").append(capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET));
        builder.append(", captivePortal=").append(capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL));
        builder.append(", downKbps=").append(capabilities.getLinkDownstreamBandwidthKbps());
        builder.append(", upKbps=").append(capabilities.getLinkUpstreamBandwidthKbps());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && capabilities.getSignalStrength() != NetworkCapabilities.SIGNAL_STRENGTH_UNSPECIFIED) {
            builder.append(", signalDbm=").append(capabilities.getSignalStrength());
        }

        LinkProperties linkProperties = connectivityManager.getLinkProperties(network);
        if (linkProperties != null && linkProperties.getMtu() > 0) {
            builder.append(", mtu=").append(linkProperties.getMtu());
        }
        return builder.toString();
    }

    private static void addTransport(NetworkCapabilities capabilities, List<String> output,
                                     int transport, String name) {
        if (capabilities.hasTransport(transport)) {
            output.add(name);
        }
    }

    private static String join(List<String> values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (builder.length() > 0) {
                builder.append('+');
            }
            builder.append(value.toUpperCase(Locale.US));
        }
        return builder.toString();
    }
}
