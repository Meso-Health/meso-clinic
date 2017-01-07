package org.watsi.uhp.models;

import org.junit.Test;

import static junit.framework.Assert.assertNotNull;

public class CheckInTest {

    @Test
    public void CheckIn_initializesTheDate() throws Exception {
        CheckIn checkIn = new CheckIn();
        assertNotNull(checkIn.getDate());
    }
}
