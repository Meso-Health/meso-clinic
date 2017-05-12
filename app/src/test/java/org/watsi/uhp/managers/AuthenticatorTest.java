package org.watsi.uhp.managers;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.BaseBundle;
import android.os.Bundle;
import android.os.Parcelable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.activities.AuthenticationActivity;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AccountManager.class, Authenticator.class, BaseBundle.class })
public class AuthenticatorTest {

    @Mock
    Context mockContext;
    @Mock
    AccountAuthenticatorResponse mockAccountAuthenticatorResponse;
    @Mock
    AccountManager mockAccountManager;
    @Mock
    Intent mockIntent;
    @Mock
    Bundle mockBundle;
    @Mock
    Account mockAccount;

    Authenticator authenticator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockStatic(BaseBundle.class);
        mockStatic(AccountManager.class);
        when(AccountManager.get(mockContext)).thenReturn(mockAccountManager);
        authenticator = new Authenticator(mockContext);
    }

    @Test
    public void getAuthToken_tokenStoredInAccountManager_immediatelyReturnsToken() throws Exception {
        String authToken = "foo";
        String authTokenType = "fooType";

        when(mockAccountManager.peekAuthToken(mockAccount, authTokenType)).thenReturn(authToken);
        whenNew(Bundle.class).withNoArguments().thenReturn(mockBundle);
        doNothing().when(mockBundle).putString(anyString(), anyString());

        Bundle result = authenticator.getAuthToken(
                mockAccountAuthenticatorResponse, mockAccount, authTokenType, null);

        assertEquals(result, mockBundle);
        verify(mockBundle, times(1)).putString(AccountManager.KEY_ACCOUNT_NAME, mockAccount.name);
        verify(mockBundle, times(1)).putString(AccountManager.KEY_ACCOUNT_TYPE, mockAccount.type);
        verify(mockBundle, times(1)).putString(AccountManager.KEY_AUTHTOKEN, authToken);
    }

    @Test
    public void getAuthToken_tokenStoredInAccountManager_promptsLogin() throws Exception {
        String authTokenType = "fooType";

        when(mockAccountManager.peekAuthToken(mockAccount, authTokenType)).thenReturn(null);
        whenNew(Bundle.class).withNoArguments().thenReturn(mockBundle);
        doNothing().when(mockBundle).putString(anyString(), anyString());
        whenNew(Intent.class)
                .withArguments(mockContext, AuthenticationActivity.class).thenReturn(mockIntent);
        when(mockIntent.putExtra(any(String.class), any(String.class))).thenReturn(mockIntent);
        when(mockIntent.putExtra(any(String.class), any(Parcelable.class))).thenReturn(mockIntent);

        Bundle result = authenticator.getAuthToken(
                mockAccountAuthenticatorResponse, mockAccount, authTokenType, null);

        assertEquals(result, mockBundle);
        verify(mockIntent, times(1)).putExtra(
                AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, mockAccountAuthenticatorResponse);
        verify(mockIntent, times(1)).putExtra(AccountManager.KEY_ACCOUNT_TYPE, mockAccount.type);
        verify(mockIntent, times(1)).putExtra(Authenticator.KEY_AUTH_TYPE, authTokenType);
        verify(mockBundle, times(1)).putParcelable(AccountManager.KEY_INTENT, mockIntent);
    }
}
