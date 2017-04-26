package org.watsi.uhp.managers;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import org.watsi.uhp.activities.AuthenticationActivity;
import org.watsi.uhp.models.User;

import java.io.IOException;

public class SessionManager {

    private final PreferencesManager mPreferencesManager;
    private final AccountManager mAccountManager;

    public SessionManager(PreferencesManager preferencesManager, AccountManager accountManager) {
        this.mPreferencesManager = preferencesManager;
        this.mAccountManager = accountManager;
    }

    /**
     * Stores the authentication token in the AccountManager and sets the user's data
     * as the PersonData when reporting Exceptions
     *
     * Also stores the username in SharedPreferences to remember the signed-in user
     * @param user Logged in user
     * @param token Authentication token
     */
    public void setUserAsLoggedIn(User user, String token) {
        Account account = new Account(user.getUsername(), Authenticator.ACCOUNT_TYPE);
        mAccountManager.setAuthToken(account, Authenticator.AUTH_TOKEN_TYPE, token);
        mPreferencesManager.setUsername(user.getUsername());
        ExceptionManager.setPersonData(String.valueOf(user.getId()), user.getUsername());
    }

    /**
     * Returns a future that resolves to a Bundle containing the logged-in users's
     * authentication token if one is stored or an Intent to launch an activity
     * to authenticate the user if a token is not stored
     * @return Fetch token result
     */
    public AccountManagerFuture<Bundle> fetchToken() {
        String username = mPreferencesManager.getUsername();
        if (username == null) return null;
        Account account = new Account(username, Authenticator.ACCOUNT_TYPE);
        return mAccountManager.getAuthToken(
                account, Authenticator.AUTH_TOKEN_TYPE, null, false, null, null);
    }

    /**
     * Can not be called from the main thread
     * @return Authentication token string
     */
    public String getToken() {
        AccountManagerFuture<Bundle> fetchTokenResult = fetchToken();
        if (fetchTokenResult != null) {
            try {
                Bundle bundle = fetchTokenResult.getResult();
                return bundle.getString(AccountManager.KEY_AUTHTOKEN);
            } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                ExceptionManager.reportException(e);
            }
        }
        return null;
    }

    /**
     * Clears the current user and starts the AuthenticationActivity
     * to prompt the new user to login
     * @param activity Context that user is logging out from
     */
    public void logout(FragmentActivity activity) {
        mPreferencesManager.clearUsername();
        activity.startActivityForResult(new Intent(activity, AuthenticationActivity.class), 0);
        // clear backstack so new user lands on CurrentPatients fragment
        activity.getSupportFragmentManager()
                .popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}
