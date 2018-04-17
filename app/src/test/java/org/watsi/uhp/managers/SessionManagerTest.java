package org.watsi.uhp.managers;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.support.v4.app.FragmentManager;

import com.rollbar.android.Rollbar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.activities.AuthenticationActivity;
import org.watsi.uhp.activities.ClinicActivity;
import org.watsi.uhp.models.AuthenticationToken;
import org.watsi.uhp.models.User;

import static junit.framework.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ExceptionManager.class, Intent.class, Rollbar.class, SessionManager.class })
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
    @Mock
    ClinicActivity mockActivity;
    @Mock
    FragmentManager mockFragmentManager;

    private SessionManager sessionManager;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockStatic(Rollbar.class);
        sessionManager = new SessionManager(mockPreferencesManager, mockAccountManager);
    }

    @Test
    public void setUserAsLoggedIn() throws Exception {
        SessionManager spiedSessionManager = spy(sessionManager);

        int id = 0;
        String username = "username";
        String password = "password";
        String token = "token";

        mockStatic(ExceptionManager.class);
        when(mockUser.getId()).thenReturn(id);
        when(mockUser.getUsername()).thenReturn(username);
        whenNew(Account.class).withArguments(username, Authenticator.ACCOUNT_TYPE)
                .thenReturn(mockAccount);
        doNothing().when(spiedSessionManager).addAccount(any(Account.class), any(String.class));

        spiedSessionManager.setUserAsLoggedIn(mockUser, password, token);

        verify(spiedSessionManager, times(1)).addAccount(mockAccount, password);
        verify(mockAccountManager, times(1)).setAuthToken(
                mockAccount, Authenticator.AUTH_TOKEN_TYPE, token);
        verify(mockPreferencesManager, times(1)).setUsername(username);
        verifyStatic(times(1));
        ExceptionManager.setPersonData(String.valueOf(id), username);
    }

    @Test
    public void addAccount_accountDoesNotExist_addsAccount() throws Exception {
        String password = "password";

        when(mockAccountManager.getAccountsByType(Authenticator.ACCOUNT_TYPE))
                .thenReturn(new Account[0]);

        sessionManager.addAccount(mockAccount, password);

        verify(mockAccountManager, times(1)).addAccountExplicitly(
                mockAccount, password, null);
    }

    @Test
    public void addAccount_accountExists_doesNotAddAccount() throws Exception {
        String password = "password";

        when(mockAccountManager.getAccountsByType(Authenticator.ACCOUNT_TYPE))
                .thenReturn(new Account[] {mockAccount});

        sessionManager.addAccount(mockAccount, password);

        verify(mockAccountManager, never()).addAccountExplicitly(
                mockAccount, password, null);
    }

    @Test
    public void fetchToken_storedUsernameIsNull_returnsNull() throws Exception {
        when(mockPreferencesManager.getUsername()).thenReturn(null);

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
    public void logout() throws Exception {
        Intent mockIntent = mock(Intent.class);

        whenNew(Intent.class).withArguments(mockActivity, AuthenticationActivity.class)
                .thenReturn(mockIntent);
        when(mockActivity.getSupportFragmentManager()).thenReturn(mockFragmentManager);

        sessionManager.logout(mockActivity);

        verify(mockPreferencesManager, times(1)).clearUsername();
        verify(mockActivity, times(1)).startActivityForResult(mockIntent, 0);
        verify(mockFragmentManager, times(1)).popBackStackImmediate(
                null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @Test
    public void logout_rollbarIsInit_clearsPersonData() throws Exception {
        when(Rollbar.isInit()).thenReturn(true);
        when(mockActivity.getSupportFragmentManager()).thenReturn(mockFragmentManager);

        sessionManager.logout(mockActivity);

        verifyStatic();
        Rollbar.setPersonData(null);
    }

    @Test
    public void logout_rollbarIsNotInit_doesNotclearPersonData() throws Exception {
        when(Rollbar.isInit()).thenReturn(false);
        when(mockActivity.getSupportFragmentManager()).thenReturn(mockFragmentManager);

        sessionManager.logout(mockActivity);

        verifyStatic(never());
        Rollbar.setPersonData(null);
    }
}
