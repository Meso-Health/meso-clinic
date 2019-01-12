package org.watsi.uhp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_authentication.error_text
import kotlinx.android.synthetic.main.activity_authentication.login_button
import kotlinx.android.synthetic.main.activity_authentication.login_password
import kotlinx.android.synthetic.main.activity_authentication.login_username
import org.watsi.device.managers.Logger
import org.watsi.device.managers.SessionManager
import org.watsi.uhp.R
import org.watsi.uhp.helpers.ActivityHelper
import org.watsi.uhp.managers.KeyboardManager
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AuthenticationActivity : DaggerAppCompatActivity() {

    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var keyboardManager: KeyboardManager
    @Inject lateinit var logger: Logger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        setTitle(R.string.authentication_activity_label)

        keyboardManager.showKeyboard(login_username)
        ActivityHelper.setupBannerIfInTrainingMode(this)

        login_button.setOnClickListener {
            login_username.clearFocus()
            login_password.clearFocus()
            keyboardManager.hideKeyboard(it)

            login_button.text = getString(R.string.logging_in)
            login_button.isEnabled = false
            sessionManager.login(login_username.text.toString(), login_password.text.toString())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        navigateToClinicActivity()
                    }, {
                        if (it is HttpException && it.code() == 401) {
                            error_text.error = getString(R.string.login_wrong_username_or_password_message)
                        } else if (it is IOException && it.message.orEmpty().contains("unexpected end of stream")) {
                            error_text.error = getString(R.string.login_offline_error)
                        } else {
                            error_text.error = getString(R.string.login_generic_failure_message)
                            logger.warning(it)
                        }
                        error_text.visibility = View.VISIBLE
                        login_button.text = getString(R.string.login_button_text)
                        login_button.isEnabled = true
                    })
        }
    }

    private fun navigateToClinicActivity() {
        // specifying CLEAR_TOP clears the AuthenticationActivity from the back-stack to prevent
        // the back button from returning the user to the login screen
        val intent = Intent(this, ClinicActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
}
