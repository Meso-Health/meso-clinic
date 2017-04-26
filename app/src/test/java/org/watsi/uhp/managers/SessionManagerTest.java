package org.watsi.uhp.managers;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.activities.AuthenticationActivity;
import org.watsi.uhp.models.AuthenticationToken;
import org.watsi.uhp.models.User;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ExceptionManager.class, Intent.class, SessionManager.class })
public class SessionManagerTest {

    @Mock
    PreferencesManager mockPreferencesManager;
    @Mock
    AuthenticationToken mockAuthenticationToken;
    @Mock
    AccountManager mockAccountManager;
    @Mock
    Account mockAccount;
    @Mock
    User mockUser;

    SessionManager sessionManager;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        sessionManager = new SessionManager(mockPreferencesManager, mockAccountManager);
    }

    @Test
    public void setUserAsLoggedIn() throws Exception {
        int id = 0;
        String username = "username";
        String token = "token";

        mockStatic(ExceptionManager.class);
        when(mockUser.getId()).thenReturn(id);
        when(mockUser.getUsername()).thenReturn(username);
        whenNew(Account.class).withArguments(username, Authenticator.ACCOUNT_TYPE)
                .thenReturn(mockAccount);

        sessionManager.setUserAsLoggedIn(mockUser, token);

        verify(mockAccountManager, times(1)).setAuthToken(
                mockAccount, Authenticator.AUTH_TOKEN_TYPE, token);
        verify(mockPreferencesManager, times(1)).setUsername(username);
        verifyStatic(times(1));
        ExceptionManager.setPersonData(String.valueOf(id), username);
    }

    @Test
    public void fetchToken_storedUsernameIsNull_returnsNull() throws Exception {
        String username = null;

        when(mockPreferencesManager.getUsername()).thenReturn(username);

        assertNull(sessionManager.fetchToken());
    }

    @Test
    public void fetchToken_storedUsernameIsNotNull_returnsStoredToken() throws Exception {
        String username = "username";

        when(mockPreferencesManager.getUsername()).thenReturn(username);
        whenNew(Account.class).withArguments(username, Authenticator.ACCOUNT_TYPE)
                .thenReturn(mockAccount);

        sessionManager.fetchToken();

        verify(mockAccountManager, times(1)).getAuthToken(
                mockAccount, Authenticator.AUTH_TOKEN_TYPE, null, false, null, null);
    }

    @Test
    public void getToken_nullFutureReturnedFromFetch_returnsNull() throws Exception {
        SessionManager spySessionManager = spy(sessionManager);

        when(spySessionManager.fetchToken()).thenReturn(null);

        assertNull(spySessionManager.getToken());
    }

    @Test
    public void getToken_futureReturnedFromFetch_returnsTokenFromBundle() throws Exception {
        String token = "token";
        AccountManagerFuture<Bundle> mockFuture = mock(AccountManagerFuture.class);
        Bundle mockBundle = mock(Bundle.class);
        SessionManager spySessionManager = spy(sessionManager);

        doReturn(mockFuture).when(spySessionManager).fetchToken();
        when(mockFuture.getResult()).thenReturn(mockBundle);
        when(mockBundle.getString(AccountManager.KEY_AUTHTOKEN)).thenReturn(token);

        assertEquals(spySessionManager.getToken(), token);
    }

    @Test
    public void logout() throws Exception {
        FragmentActivity mockActivity = mock(FragmentActivity.class);
        Intent mockIntent = mock(Intent.class);
        FragmentManager mockFragmentManager = mock(FragmentManager.class);

        whenNew(Intent.class).withArguments(mockActivity, AuthenticationActivity.class)
                .thenReturn(mockIntent);
        when(mockActivity.getSupportFragmentManager()).thenReturn(mockFragmentManager);

        sessionManager.logout(mockActivity);

        verify(mockPreferencesManager, times(1)).clearUsername();
        verify(mockActivity, times(1)).startActivityForResult(mockIntent, 0);
        verify(mockFragmentManager, times(1)).popBackStackImmediate(
                null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}
