package org.watsi.uhp.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.rollbar.android.Rollbar;

import org.watsi.uhp.R;
import org.watsi.uhp.activities.MainActivity;
import org.watsi.uhp.api.ApiService;
import org.watsi.uhp.managers.KeyboardManager;
import org.watsi.uhp.managers.NavigationManager;

import java.util.HashMap;
import java.util.Map;

import me.philio.pinentry.PinEntryView;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        getActivity().setTitle(R.string.login_fragment_label);
        setHasOptionsMenu(true);
        getActivity().invalidateOptionsMenu();

        final View view = inflater.inflate(R.layout.fragment_login, container, false);

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
                KeyboardManager.hideKeyboard(view, getContext());
                final String username = usernameView.getText().toString();
                final String password = passwordView.getText().toString();
                final ProgressDialog spinner = new ProgressDialog(getContext(), ProgressDialog.STYLE_SPINNER);
                spinner.setCancelable(false);
                spinner.setMessage(getContext().getString(R.string.login_progress_message));
                spinner.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Response response = ApiService.login(username, password, getContext());
                        spinner.dismiss();
                        if (response == null || !response.isSuccessful()) {
                            StringBuilder sb = new StringBuilder();
                            if (response == null) {
                                sb.append(getContext().getString(R.string.login_generic_failure_message));
                            } else {
                                if (response.code() == 401) {
                                    sb.append(getContext().getString(R.string.login_wrong_password_message));
                                } else {
                                    Map<String, String> errorParams = new HashMap<>();
                                    errorParams.put("username", username);
                                    errorParams.put("http_status_code", String.valueOf(response.code()));
                                    Rollbar.reportMessage("Login failed", "warning", errorParams);
                                    sb.append(getContext().getString(R.string.login_generic_failure_message));
                                }
                            }
                            final String errorMessage = sb.toString();
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(
                                            getActivity().getApplicationContext(),
                                            errorMessage,
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            });
                        } else {
                            new NavigationManager(getActivity()).setCurrentPatientsFragment();
                        }
                    }
                }).start();
            }
        });

        return view;
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
            if (usernameEdit.getText().toString().isEmpty() ||
                    passwordEdit.getText().toString().isEmpty()) {
                loginButton.setEnabled(false);
            } else {
                loginButton.setEnabled(true);
            }
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_logout).setVisible(false);
    }
}
