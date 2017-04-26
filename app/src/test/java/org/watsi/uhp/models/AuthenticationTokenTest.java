package org.watsi.uhp.models;

import com.rollbar.android.Rollbar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.managers.Clock;

import java.util.Calendar;
import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Rollbar.class)
public class AuthenticationTokenTest {

    private AuthenticationToken authToken;

    private Date currentTime = Calendar.getInstance().getTime();

    @Before
    public void setup() {
        Clock.setTime(currentTime);
        authToken = new AuthenticationToken();
    }

    @Test
    public void daysTillExpiration_inOneHour() throws Exception {
        long timeTillExpiry = 60 * 60 * 1000L;
        Date expiry = new Date(currentTime.getTime() + timeTillExpiry);
        authToken.setExpiresAt(Clock.ISO_DATE_FORMAT.format(expiry));

        assertEquals(authToken.timeTillExpiration(), timeTillExpiry);
    }

    @Test
    public void daysTillExpiration_oneHourAgo() throws Exception {
        long timeTillExpiry = -60 * 60 * 1000L;
        Date expiry = new Date(currentTime.getTime() + timeTillExpiry);
        authToken.setExpiresAt(Clock.ISO_DATE_FORMAT.format(expiry));

        assertEquals(authToken.timeTillExpiration(), timeTillExpiry);
    }

    @Test
    public void daysTillExpiration_invalidExpiresAtString_returnsNegativeOne() throws Exception {
        mockStatic(Rollbar.class);
        doNothing().when(Rollbar.class);

        authToken.setExpiresAt("foo");

        assertEquals(authToken.timeTillExpiration(), -1);
    }

    @Test
    public void shouldRefreshToken_lessThanSevenDaysTillExpiry_returnsTrue() throws Exception {
        AuthenticationToken spyToken = spy(authToken);

        doReturn(0L).when(spyToken).timeTillExpiration();
        assertTrue(spyToken.shouldRefreshToken());

        doReturn(AuthenticationToken.REFRESH_TOKEN_THRESHOLD - 1L).when(spyToken).timeTillExpiration();
        assertTrue(spyToken.shouldRefreshToken());
    }

    @Test
    public void shouldRefreshToken_greaterThanSevenDaysTillExpiry_returnsFalse() throws Exception {
        AuthenticationToken spyToken = spy(authToken);

        doReturn(AuthenticationToken.REFRESH_TOKEN_THRESHOLD + 1L).when(spyToken).timeTillExpiration();
        assertFalse(spyToken.shouldRefreshToken());

        doReturn(AuthenticationToken.REFRESH_TOKEN_THRESHOLD * 2L).when(spyToken).timeTillExpiration();
        assertFalse(spyToken.shouldRefreshToken());
    }
}
