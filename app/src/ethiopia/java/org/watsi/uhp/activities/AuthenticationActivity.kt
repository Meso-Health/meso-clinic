package org.watsi.uhp.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
import java.lang.RuntimeException
import javax.inject.Inject

class AuthenticationActivity : LocaleAwareActivity() {

    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var keyboardManager: KeyboardManager
    @Inject lateinit var logger: Logger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (sessionManager.currentAuthenticationToken()?.token != null) {
            navigateToClinicActivity()
        }
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
                        } else if (it is SessionManager.PermissionException) {
                            error_text.error = getString(R.string.login_permission_error)
                        } else if ((it is RuntimeException && it.message.orEmpty().contains("Unable to resolve host"))
                                || (it is IOException && it.message.orEmpty().contains("unexpected end of stream"))) {
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        val switchLanguageItem = menu.findItem(R.id.menu_switch_language)
        switchLanguageItem.isVisible = true
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_switch_language -> {
                localeManager.toggleLocale(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
