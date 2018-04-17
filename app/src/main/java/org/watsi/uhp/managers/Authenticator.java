package org.watsi.uhp.managers;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.common.base.Strings;

import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.activities.OldAuthenticationActivity;

/**
 * A component used for working with Android accounts.
 *
 * Our use-case only requires implementing the getAuthToken method which allows us to store
 * and fetch User's authentication tokens using Android's AccountManager class
 */
public class Authenticator extends AbstractAccountAuthenticator {

    public static final String AUTH_TOKEN_TYPE = "sync";
    public static final String AUTH_TOKEN_LABEL = "Sync";
    public static final String ACCOUNT_TYPE = BuildConfig.APPLICATION_ID;

    protected static final String KEY_AUTH_TYPE = "authType";

    private final Context mContext;
    private final AccountManager mAccountManager;

    public Authenticator(Context context) {
        super(context);
        this.mContext = context;
        this.mAccountManager = AccountManager.get(mContext);
    }

    // unsupported
    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        throw new UnsupportedOperationException();
    }

    // do not support creating accounts from the Android accounts settings menu
    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response,
                             String accountType,
                             String authTokenType,
                             String[] requiredFeatures,
                             Bundle options) throws NetworkErrorException {
        return null;
    }

    // not used
    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response,
                                     Account account,
                                     Bundle options) throws NetworkErrorException {
        return null;
    }

    /**
     * Called by the AccountManager whenever an authorization token is requested.
     *
     * If a token is associated with an account, it returns that token.
     *
     * If no token is available and it is being called from an activity, it redirects
     * the user to the AuthenticationActivity to setUserAsLoggedIn. If it is being called from the
     * SyncAdapter and a valid token cannot be returned, it logs and return an informative
     * error.
     *
     * @param response
     * @param account
     * @param authTokenType
     * @param options
     * @return
     * @throws NetworkErrorException
     */
    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response,
                               Account account,
                               String authTokenType,
                               Bundle options) throws NetworkErrorException {

        String authToken = mAccountManager.peekAuthToken(account, authTokenType);

        if (!Strings.isNullOrEmpty(authToken)) {
            // AuthenticationToken is present, so return it directly
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            return result;
        } else {
            // no stored AuthenticationToken, so prompt user to login via the AuthenticationActivity
            final Intent intent = new Intent(mContext, OldAuthenticationActivity.class);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
            intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            intent.putExtra(KEY_AUTH_TYPE, authTokenType);
            final Bundle bundle = new Bundle();
            bundle.putParcelable(AccountManager.KEY_INTENT, intent);
            return bundle;
        }
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return AUTH_TOKEN_LABEL;
    }

    // not used
    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response,
                                    Account account,
                                    String authTokenType,
                                    Bundle options) throws NetworkErrorException {
        return null;
    }

    // not used
    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response,
                              Account account,
                              String[] features) throws NetworkErrorException {
        return null;
    }
}
