package org.watsi.uhp.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_authentication.android_version
import kotlinx.android.synthetic.main.activity_authentication.app_version
import kotlinx.android.synthetic.main.activity_authentication.login_button
import kotlinx.android.synthetic.main.activity_authentication.login_password
import kotlinx.android.synthetic.main.activity_authentication.login_username
import kotlinx.android.synthetic.main.activity_authentication.password_container
import kotlinx.android.synthetic.main.activity_authentication.update_notification
import org.watsi.device.managers.Logger
import org.watsi.device.managers.PreferencesManager
import org.watsi.device.managers.SessionManager
import org.watsi.domain.usecases.DeleteUserDataUseCase
import org.watsi.uhp.BuildConfig
import org.watsi.uhp.R
import org.watsi.uhp.helpers.NetworkErrorHelper
import org.watsi.uhp.managers.AppUpdateManager
import org.watsi.uhp.managers.KeyboardManager
import javax.inject.Inject

class AuthenticationActivity : LocaleAwareActivity() {
    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var keyboardManager: KeyboardManager
    @Inject lateinit var logger: Logger
    @Inject lateinit var deleteUserDataUseCase: DeleteUserDataUseCase
    @Inject lateinit var preferencesManager: PreferencesManager
    private lateinit var appUpdateManager: AppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (sessionManager.currentAuthenticationToken()?.token != null) {
            navigateToClinicActivity()
        }
        setContentView(R.layout.activity_authentication)
        setTitle(R.string.authentication_activity_label)

        keyboardManager.showKeyboard(login_username)

        login_button.setOnClickListener {
            login_username.clearFocus()
            login_password.clearFocus()
            keyboardManager.hideKeyboard(it)

            login_button.text = getString(R.string.logging_in)
            login_button.isEnabled = false
            Completable.concatArray(
                sessionManager.login(login_username.text.toString(), login_password.text.toString()),
                Completable.fromAction {
                    if (sessionManager.shouldClearUserData()) {
                        deleteUserDataUseCase.execute().blockingAwait()
                        preferencesManager.updateMembersPageKey(null)
                    }
                }.onErrorComplete {
                    logger.error(it)
                    true
                }
            ).observeOn(AndroidSchedulers.mainThread()).subscribe({
                navigateToClinicActivity()
            }, this::handleLoginFailure)
        }

        app_version.text = getString(R.string.app_version, BuildConfig.VERSION_NAME)
        android_version.text = getString(R.string.android_version, android.os.Build.VERSION.RELEASE)
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager = AppUpdateManager(
            activity = this,
            logger = logger
        )
        appUpdateManager.setOnUpdateAvailable { appUpdateInfo ->
            // If there is an update, show the update notification bar and set what happens when it is clicked.
            update_notification.visibility = View.VISIBLE
            update_notification.setOnClickListener {
                appUpdateManager.requestUpdate(appUpdateInfo)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        appUpdateManager.tearDown()
    }

    private fun handleLoginFailure(throwable: Throwable) {
        val throwable = throwable.cause ?: throwable

        when {
            throwable is SessionManager.PermissionException -> {
                password_container.error = getString(R.string.login_permission_error)
            }
            NetworkErrorHelper.isHttpUnauthorized(throwable) -> {
                password_container.error = getString(R.string.login_wrong_username_or_password_message)
            }
            NetworkErrorHelper.isPhoneOfflineError(throwable) -> {
                password_container.error = getString(R.string.login_phone_offline_error)
            }
            NetworkErrorHelper.isServerOfflineError(throwable) -> {
                password_container.error = getString(R.string.login_server_offline_error)
            }
            NetworkErrorHelper.isPoorConnectivityError(throwable) -> {
                password_container.error = getString(R.string.login_connectivity_error)
            }
            else -> {
                // login failed due to server error
                //  this path should only be used for server 500s, if we are seeing
                //  exceptions for other reasons being caught here, we should add
                //  them to the appropriate cases above
                password_container.error = getString(R.string.login_generic_failure_message)
                logger.error(throwable)
            }
        }
        login_button.text = getString(R.string.login_button_text)
        login_button.isEnabled = true
    }

    private fun navigateToClinicActivity() {
        // specifying CLEAR_TOP clears the AuthenticationActivity from the back-stack to prevent
        // the back button from returning the user to the login screen
        val intent = Intent(this, ClinicActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (BuildConfig.ENABLE_LANGUAGE_SWITCH) {
            menuInflater.inflate(R.menu.main, menu)
            val switchLanguageItem = menu.findItem(R.id.menu_switch_language)
            switchLanguageItem.isVisible = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_switch_language -> {
                localeManager.setLocaleConfirmationDialog(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
