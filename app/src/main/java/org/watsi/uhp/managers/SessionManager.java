package org.watsi.uhp.managers;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.rollbar.android.Rollbar;

import org.watsi.domain.entities.User;
import org.watsi.uhp.activities.AuthenticationActivity;
import org.watsi.uhp.activities.ClinicActivity;

import java.util.Arrays;

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
     * @param password User's password
     * @param token Authentication token
     */
    public void setUserAsLoggedIn(User user, String password, String token) {
        Account account = new Account(user.getUsername(), Authenticator.ACCOUNT_TYPE);
        addAccount(account, password);

        mAccountManager.setAuthToken(account, Authenticator.AUTH_TOKEN_TYPE, token);
        mPreferencesManager.setUsername(user.getUsername());
        ExceptionManager.setPersonData(String.valueOf(user.getId()), user.getUsername());
    }

    public String getCurrentLoggedInUsername() {
        return mPreferencesManager.getUsername();
    }

    /**
     * Creates an account using AccountManager if it does not already exist.
     *
     * @param account The account
     * @param password User's password
     */
    public void addAccount(Account account, String password) {
        Account[] accounts = mAccountManager.getAccountsByType(Authenticator.ACCOUNT_TYPE);
        if (!Arrays.asList(accounts).contains(account)) {
            mAccountManager.addAccountExplicitly(account, password, null);
        }
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
     * Clears the current user and starts the AuthenticationActivity
     * to prompt the new user to login
     * @param activity Context that user is logging out from
     */
    public void logout(ClinicActivity activity) {
        mPreferencesManager.clearUsername();
        if (Rollbar.isInit()) Rollbar.setPersonData(null);
        activity.startActivityForResult(new Intent(activity, AuthenticationActivity.class), 0);
        // clear backstack so new user lands on CurrentPatients fragment
        activity.getSupportFragmentManager()
                .popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}
