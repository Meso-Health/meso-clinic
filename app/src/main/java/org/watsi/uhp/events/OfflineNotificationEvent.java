package org.watsi.uhp.events;

public class OfflineNotificationEvent {
    private final boolean mOffline;

    public OfflineNotificationEvent(boolean isOffline) {
        this.mOffline = isOffline;
    }

    public boolean isOffline() {
        return mOffline;
    }
}
