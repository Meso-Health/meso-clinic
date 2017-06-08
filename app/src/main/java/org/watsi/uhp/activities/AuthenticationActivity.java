package org.watsi.uhp.activities;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.watsi.uhp.R;
import org.watsi.uhp.api.ApiService;
import org.watsi.uhp.managers.Authenticator;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.KeyboardManager;
import org.watsi.uhp.managers.PreferencesManager;
import org.watsi.uhp.managers.SessionManager;
import org.watsi.uhp.models.AuthenticationToken;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import me.philio.pinentry.PinEntryView;
import retrofit2.Response;

public class AuthenticationActivity extends AccountAuthenticatorActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        ((Toolbar) findViewById(R.id.toolbar)).setTitle(R.string.authentication_activity_label);

        final View view = findViewById(R.id.authentication_activity);

        final EditText usernameView = (EditText) view.findViewById(R.id.login_username);
        final PinEntryView passwordView = (PinEntryView) view.findViewById(R.id.login_password);
        Button loginButton = (Button) view.findViewById(R.id.login_button);

        TextWatcher watcher = new LoginTextWatcher(usernameView, passwordView, loginButton);

        usernameView.addTextChangedListener(watcher);
        passwordView.addTextChangedListener(watcher);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usernameView.clearFocus();
                passwordView.clearFocus();
                KeyboardManager.hideKeyboard(view, getBaseContext());
                final String username = usernameView.getText().toString();
                final String password = passwordView.getText().toString();
                final ProgressDialog spinner = new ProgressDialog(
                        AuthenticationActivity.this, ProgressDialog.STYLE_SPINNER);
                spinner.setCancelable(false);
                spinner.setMessage(getBaseContext().getString(R.string.login_progress_message));
                spinner.show();

                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        authenticate(username, password, spinner);
                        return null;
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        KeyboardManager.focusAndShowKeyboard(usernameView, this);
    }

    private static class AuthenticateAsyncTask extends AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... params) {
            final String = (String) params[0];

            authenticate(username, password, spinner);
            return null;
        }
    }

    private void authenticate(String username, String password, ProgressDialog spinner) {
        Response<AuthenticationToken> response =
                ApiService.authenticate(username, password);
        spinner.dismiss();
        if (response == null || !response.isSuccessful()) {
            String errorMessage;

            if (response == null) {
                errorMessage = getString(R.string.login_offline_error);
            } else if (response.code() == 401) {
                errorMessage = getString(R.string.login_wrong_password_message);
            } else {
                errorMessage = getString(R.string.login_generic_failure_message);
                Map<String,String> warningDetails = new HashMap<>();
                warningDetails.put("response.code", String.valueOf(response.code()));
                warningDetails.put("response.message", response.message());
                ExceptionManager.reportMessage(
                        "Unexpected login failure",
                        ExceptionManager.MESSAGE_LEVEL_WARNING,
                        warningDetails);
            }

            final String errorMessageFinal = errorMessage;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(
                            getApplicationContext(),
                            errorMessageFinal,
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        } else {
            AuthenticationToken authToken = response.body();
            Account account = new Account(username, Authenticator.ACCOUNT_TYPE);
            AccountManager accountManager = AccountManager.get(this);

            // create account using AccountManager if it does not already exist
            Account[] accounts = accountManager.getAccountsByType(Authenticator.ACCOUNT_TYPE);
            if (!Arrays.asList(accounts).contains(account)) {
                accountManager.addAccountExplicitly(account, password, null);
            }

            new SessionManager(new PreferencesManager(this), accountManager)
                    .setUserAsLoggedIn(authToken.getUser(), authToken.getToken());

            Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, username);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, Authenticator.ACCOUNT_TYPE);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken.getToken());
            setAccountAuthenticatorResult(result);
            setResult(RESULT_OK);
            finish();
        }
    }

    private class LoginTextWatcher implements TextWatcher {

        private EditText usernameEdit;
        private PinEntryView passwordEdit;
        private Button loginButton;

        private LoginTextWatcher(EditText usernameEdit, PinEntryView passwordEdit, Button loginButton) {
            this.usernameEdit = usernameEdit;
            this.passwordEdit = passwordEdit;
            this.loginButton = loginButton;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // no-op
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // no-op
        }

        @Override
        public void afterTextChanged(Editable s) {
            String pin = passwordEdit.getText().toString();
            if (usernameEdit.getText().toString().isEmpty() || pin.length() < 6) {
                loginButton.setEnabled(false);
            } else {
                loginButton.setEnabled(true);
            }
        }
    }
}
