package android.security;

import android.annotation.SuppressLint;

// Necessary to get Roboelectric tests to allow OkHttp requests (only compiled in test code)
//  ref: https://github.com/square/okhttp/issues/2533#issuecomment-223093100
public class NetworkSecurityPolicy {
    private static final NetworkSecurityPolicy INSTANCE = new NetworkSecurityPolicy();

    @SuppressLint("NewApi")
    public static NetworkSecurityPolicy getInstance() { return INSTANCE; }

    public boolean isCleartextTrafficPermitted() { return true; }
}