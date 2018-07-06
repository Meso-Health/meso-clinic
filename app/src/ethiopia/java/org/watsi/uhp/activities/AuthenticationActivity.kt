package org.watsi.uhp.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_authentication.error_text
import kotlinx.android.synthetic.main.activity_authentication.login_button
import kotlinx.android.synthetic.main.activity_authentication.login_password
import kotlinx.android.synthetic.main.activity_authentication.login_username
import me.philio.pinentry.PinEntryView
import org.watsi.device.managers.Logger
import org.watsi.device.managers.SessionManager
import org.watsi.uhp.BaseApplication
import org.watsi.uhp.R
import org.watsi.uhp.helpers.ActivityHelper
import org.watsi.uhp.managers.KeyboardManager
import org.watsi.uhp.managers.LocaleManager
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AuthenticationActivity : DaggerAppCompatActivity() {

    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var keyboardManager: KeyboardManager
    @Inject lateinit var logger: Logger
    lateinit var localeManager: LocaleManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        setTitle(R.string.authentication_activity_label)

        keyboardManager.showKeyboard(login_username)
        ActivityHelper.setupBannerIfInTrainingMode(this)

        val watcher = LoginTextWatcher(login_username, login_password, login_button)

        login_username.addTextChangedListener(watcher)
        login_password.addTextChangedListener(watcher)

        login_button.setOnClickListener {
            login_username.clearFocus()
            login_password.clearFocus()
            keyboardManager.hideKeyboard(it)

            sessionManager.login(login_username.text.toString(), login_password.text.toString())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        navigateToClinicActivity()
                    }, {
                        if (it is HttpException && it.code() == 401) {
                            error_text.text = getString(R.string.login_wrong_password_message)
                        } else if (it is IOException && it.message.orEmpty().contains("unexpected end of stream")) {
                            error_text.text = getString(R.string.login_offline_error)
                        } else {
                            error_text.text = getString(R.string.login_generic_failure_message)
                            logger.warning(it)
                        }
                        error_text.visibility = View.VISIBLE
                    })
        }
    }

    override fun attachBaseContext(base: Context) {
        localeManager = (base.applicationContext as BaseApplication).localeManager
        super.attachBaseContext(localeManager.createLocalizedContext(base))
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
            error_text.visibility = View.GONE
            val pin = passwordEdit.text.toString()
            loginButton.isEnabled = !(usernameEdit.text.toString().isEmpty() || pin.length < 6)
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
