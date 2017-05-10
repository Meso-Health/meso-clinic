package org.watsi.uhp.basetests;

public class ActivityTest {
    private static final long WAIT_FOR_UI_TO_UPDATE = 1000L;

    protected void waitForUIToUpdate() {
        try {
            Thread.sleep(WAIT_FOR_UI_TO_UPDATE);
        } catch (Exception ignored) {
        }
    }
}
