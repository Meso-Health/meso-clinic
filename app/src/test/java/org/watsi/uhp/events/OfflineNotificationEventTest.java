package org.watsi.uhp.events;

import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class OfflineNotificationEventTest {

    @Test
    public void isOffline_offlineEvent() throws Exception {
        OfflineNotificationEvent offlineEvent = new OfflineNotificationEvent(true);
        assertTrue(offlineEvent.isOffline());

    }

    @Test
    public void isOffline_onlineEvent() throws Exception {
        OfflineNotificationEvent onlineEvent = new OfflineNotificationEvent(false);
        assertFalse(onlineEvent.isOffline());
    }
}
