package org.watsi.uhp.managers

import android.app.Activity
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import org.watsi.device.managers.Logger

class AppUpdateManager(
    val activity: Activity,
    val logger: Logger
) {
    private var appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)
    private var installStateUpdatedListener: InstallStateUpdatedListener

    init {
        // TODO: We can also do some more fancy stuff like don't check update more once in a single day, etc.
        installStateUpdatedListener = InstallStateUpdatedListener { installState ->
            val installStatus = installState?.installStatus()
            if (installStatus == InstallStatus.DOWNLOADED) {
                logger.info("App is downloaded. Completing installation now...")
                appUpdateManager.completeUpdate()
            } else {
                logger.warning("InstallState status was: $installStatus")
            }
        }
        appUpdateManager.registerListener(installStateUpdatedListener)
    }

    fun setOnUpdateAvailable(onUpdateAvailable: (appUpdateInfo: AppUpdateInfo) -> Unit) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            val availability = appUpdateInfo.updateAvailability()
            if (availability == UpdateAvailability.UPDATE_AVAILABLE &&
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {   //  check for the type of update flow you want
                logger.info("Update is available...")
                onUpdateAvailable(appUpdateInfo)
            }
        }
        appUpdateManager.appUpdateInfo.addOnFailureListener {
            logger.error(it)
        }
    }

    fun tearDown() {
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }

    companion object {
        private const val REQUEST_CODE_FLEXIBLE_UPDATE = 17362
    }

    fun requestUpdate(appUpdateInfo: AppUpdateInfo?) {
        appUpdateManager.startUpdateFlowForResult(
            appUpdateInfo,
            AppUpdateType.IMMEDIATE,
            activity,
            REQUEST_CODE_FLEXIBLE_UPDATE
        )
    }
}
