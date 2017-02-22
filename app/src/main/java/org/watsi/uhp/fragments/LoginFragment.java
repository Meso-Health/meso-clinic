package org.watsi.uhp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.watsi.uhp.R;
import org.watsi.uhp.activities.MainActivity;
import org.watsi.uhp.api.ApiService;
import org.watsi.uhp.managers.KeyboardManager;

public class LoginFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        getActivity().setTitle(R.string.login_fragment_label);

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        final EditText usernameView = (EditText) view.findViewById(R.id.login_username);
        final EditText passwordView = (EditText) view.findViewById(R.id.login_password);
        Button loginButton = (Button) view.findViewById(R.id.login_button);

        TextWatcher watcher = new LoginTextWatcher(usernameView, passwordView, loginButton);

        usernameView.addTextChangedListener(watcher);
        passwordView.addTextChangedListener(watcher);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usernameView.clearFocus();
                passwordView.clearFocus();
                KeyboardManager.hideKeyboard(getContext());
                final String username = usernameView.getText().toString();
                final String password = passwordView.getText().toString();
                // TODO: put up loading spinner
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (ApiService.login(username, password, getContext())) {
                            ((MainActivity) getActivity()).setCurrentPatientsFragment(false);
                        } else {
                            // TODO: handle failed login
                        }
                    }
                }).start();
            }
        });

        return view;
    }

    private class LoginTextWatcher implements TextWatcher {

        private EditText usernameEdit;
        private EditText passwordEdit;
        private Button loginButton;

        private LoginTextWatcher(EditText usernameEdit, EditText passwordEdit, Button loginButton) {
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
}
