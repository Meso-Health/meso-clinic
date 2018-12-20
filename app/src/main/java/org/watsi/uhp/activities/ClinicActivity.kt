package org.watsi.uhp.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v4.app.ActivityCompat
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import dagger.android.support.DaggerAppCompatActivity
import net.hockeyapp.android.UpdateManager
import org.watsi.device.managers.SessionManager
import org.watsi.uhp.BuildConfig
import org.watsi.uhp.R
import org.watsi.uhp.fragments.CurrentPatientsFragment
import org.watsi.uhp.helpers.ActivityHelper
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.services.BaseService
import org.watsi.uhp.services.DeleteSyncedPhotosService
import org.watsi.uhp.services.FetchMemberPhotosService
import org.watsi.uhp.services.FetchService
import org.watsi.uhp.services.SyncDataService
import org.watsi.uhp.services.SyncPhotosService
import javax.inject.Inject

class ClinicActivity : DaggerAppCompatActivity() {

    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var navigationManager: NavigationManager

    companion object {
        private val FETCH_SERVICE_JOB_ID = 0
        private val FETCH_MEMBER_PHOTOS_SERVICE_JOB_ID = 1
        private val SYNC_DATA_SERVICE_JOB_ID = 2
        private val SYNC_PHOTOS_SERVICE_JOB_ID = 3
        private val DELETE_SYNCED_PHOTOS_SERVICE_JOB_ID = 4
        val requiredPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.INTERNET)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clinic)

        ActivityHelper.setupBannerIfInTrainingMode(this)
        startServices()

        navigationManager.goTo(CurrentPatientsFragment())
    }

    /**
     * This method gets called after onResume and will get called after both onCreate and
     * after onActivityResult which will ensure we force setUserAsLoggedIn when necessary
     */
    override fun onPostResume() {
        super.onPostResume()

        val hasPermissions = requiredPermissions.all {
            ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!hasPermissions) {
            ActivityCompat.requestPermissions(this, requiredPermissions, 0)
        } else if (sessionManager.currentToken() == null) {
            navigateToAuthenticationActivity()
        }
    }

    override fun onStart() {
        super.onStart()

        if (BuildConfig.SHOULD_CHECK_FOR_UPDATES) checkForUpdates()
    }

    public override fun onPause() {
        super.onPause()
        UpdateManager.unregister()
    }

    public override fun onDestroy() {
        super.onDestroy()
        UpdateManager.unregister()
    }

    private fun startServices() {
        BaseService.schedule(FETCH_SERVICE_JOB_ID, this, FetchService::class.java)
        BaseService.schedule(FETCH_MEMBER_PHOTOS_SERVICE_JOB_ID, this, FetchMemberPhotosService::class.java)
        BaseService.schedule(SYNC_DATA_SERVICE_JOB_ID, this, SyncDataService::class.java)
        BaseService.schedule(SYNC_PHOTOS_SERVICE_JOB_ID, this, SyncPhotosService::class.java)
        BaseService.schedule(DELETE_SYNCED_PHOTOS_SERVICE_JOB_ID, this, DeleteSyncedPhotosService::class.java)
    }

    private fun checkForUpdates() {
        UpdateManager.register(this, BuildConfig.HOCKEYAPP_APP_ID)
    }

    fun setSoftInputModeToNothing() {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }

    fun setSoftInputModeToPan() {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onBackPressed() {
        navigationManager.goBack()
    }

    /**
     * Helper method for configuring the toolbar from a Fragment
     *
     * @param title String to use as the title
     * @param homeIconId ID of a DrawableRes to use as the up navigation affordance
     *                   Pass null if an up navigation affordance should not be displayed
     *                   and pass 0 to use the theme default (back arrow)
     */
    fun setToolbar(title: String, @DrawableRes homeIconId: Int?) {
        setTitle(title)
        setToolbarHomeIcon(homeIconId)
    }

    /**
     * Sets the toolbar to be flat and title-less.
     */
    fun setToolbarMinimal(@DrawableRes homeIconId: Int?) {
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.elevation = 0.toFloat()
        setToolbarHomeIcon(homeIconId)
    }

    /**
     * Resets the changes made by setToolbarMinimal
     */
    fun resetToolbarMinimal() {
        supportActionBar?.setDisplayShowTitleEnabled(true)
        //TODO: 4 is the default material design elevation for action bars, but ours is set to 12 for some reason.
        supportActionBar?.elevation = 12.toFloat()
    }

    private fun setToolbarHomeIcon(@DrawableRes homeIconId: Int?) {
        supportActionBar?.setDisplayHomeAsUpEnabled(homeIconId != null)
        homeIconId?.let{ supportActionBar?.setHomeAsUpIndicator(it) }
    }

    fun navigateToAuthenticationActivity() {
        startActivity(Intent(this, AuthenticationActivity::class.java))
    }
}