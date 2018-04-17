package org.watsi.uhp.activities

import android.accounts.AccountManager
import android.app.ProgressDialog
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import dagger.android.support.DaggerAppCompatActivity
import me.philio.pinentry.PinEntryView
import org.watsi.uhp.R
import org.watsi.uhp.api.ApiService
import org.watsi.uhp.helpers.ActivityHelper
import org.watsi.uhp.managers.ExceptionManager
import org.watsi.uhp.managers.KeyboardManager
import org.watsi.uhp.managers.PreferencesManager
import org.watsi.uhp.managers.SessionManager
import java.util.HashMap

class AuthenticationActivity : DaggerAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        (findViewById<View>(R.id.toolbar) as Toolbar).setTitle(R.string.authentication_activity_label)

        val view = findViewById<View>(R.id.authentication_activity)

        val usernameView = view.findViewById<View>(R.id.login_username) as EditText
        val passwordView = view.findViewById<View>(R.id.login_password) as PinEntryView
        val loginButton = view.findViewById<View>(R.id.login_button) as Button

        val watcher = LoginTextWatcher(usernameView, passwordView, loginButton)

        usernameView.addTextChangedListener(watcher)
        passwordView.addTextChangedListener(watcher)

        loginButton.setOnClickListener {
            usernameView.clearFocus()
            passwordView.clearFocus()
            KeyboardManager.hideKeyboard(view, baseContext)
            val username = usernameView.text.toString()
            val password = passwordView.text.toString()
            val spinner = ProgressDialog(this, ProgressDialog.STYLE_SPINNER)
            spinner.setCancelable(false)
            spinner.setMessage(baseContext.getString(R.string.login_progress_message))
            spinner.show()

            object : AsyncTask<Void, Void, Void>() {
                override fun doInBackground(vararg params: Void): Void? {
                    authenticate(username, password, spinner)
                    return null
                }
            }
        }

        KeyboardManager.focusAndShowKeyboard(usernameView, this)
        ActivityHelper.setupBannerIfInTrainingMode(this)
    }

    private fun authenticate(username: String, password: String, spinner: ProgressDialog) {
        val response = ApiService.authenticate(username, password)
        spinner.dismiss()
        if (response == null || !response.isSuccessful) {

            val errorMessage = when {
                response == null -> getString(R.string.login_offline_error)
                response.code() == 401 -> getString(R.string.login_wrong_password_message)
                else -> getString(R.string.login_generic_failure_message)
            }

            val warningDetails = HashMap<String, String>()
            warningDetails["errorMessage"] = errorMessage
            if (response != null) {
                warningDetails["response.code"] = response.code().toString()
                warningDetails["response.message"] = response.message()
            }
            ExceptionManager.reportMessage(
                    "Login Failure", ExceptionManager.MESSAGE_LEVEL_WARNING, warningDetails)

            runOnUiThread {
                Toast.makeText(
                        applicationContext,
                        errorMessage,
                        Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            val authToken = response.body()
            val accountManager = AccountManager.get(this)

            SessionManager(PreferencesManager(this), accountManager)
                    .setUserAsLoggedIn(authToken!!.user, password, authToken.token)
//            val result = Bundle()
//            result.putString(AccountManager.KEY_ACCOUNT_NAME, username)
//            result.putString(AccountManager.KEY_ACCOUNT_TYPE, Authenticator.ACCOUNT_TYPE)
//            result.putString(AccountManager.KEY_AUTHTOKEN, authToken.token)
//            setAccountAuthenticatorResult(result)
//            setResult(Activity.RESULT_OK)
//            finish()
        }
    }

    inner class LoginTextWatcher constructor(private val usernameEdit: EditText,
                                             private val passwordEdit: PinEntryView,
                                             private val loginButton: Button) : TextWatcher {

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            // no-op
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            // no-op
        }

        override fun afterTextChanged(s: Editable) {
            val pin = passwordEdit.text.toString()
            loginButton.isEnabled = !(usernameEdit.text.toString().isEmpty() || pin.length < 6)
        }
    }
}
