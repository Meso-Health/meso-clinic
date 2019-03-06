package org.watsi.uhp.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.v4.app.ActivityCompat
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import dagger.android.support.DaggerAppCompatActivity
import org.watsi.device.managers.SessionManager
import org.watsi.uhp.BaseApplication
import org.watsi.uhp.R
import org.watsi.uhp.fragments.HomeFragment
import org.watsi.uhp.helpers.ActivityHelper
import org.watsi.uhp.managers.LocaleManager
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
    lateinit var localeManager: LocaleManager

    companion object {
        private val FETCH_DATA_SERVICE_JOB_ID = 0
        private val FETCH_MEMBER_PHOTOS_SERVICE_JOB_ID = 1
        private val SYNC_DATA_SERVICE_JOB_ID = 2
        private val SYNC_PHOTOS_SERVICE_JOB_ID = 3
        private val DELETE_SYNCED_PHOTOS_SERVICE_JOB_ID = 4
        val requiredPermissions = arrayOf(Manifest.permission.INTERNET, Manifest.permission.CAMERA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clinic)

        ActivityHelper.setupBannerIfInTrainingMode(this)
        startServices()

        navigationManager.goTo(HomeFragment())
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
        } else if (sessionManager.currentAuthenticationToken() == null) {
            navigateToAuthenticationActivity()
        }
    }

    override fun attachBaseContext(base: Context) {
        // pull LocaleManager off of Application because Activity is not injected when this is called
        localeManager = (base.applicationContext as BaseApplication).localeManager
        super.attachBaseContext(localeManager.createLocalizedContext(base))
    }

    fun startServices() {
        BaseService.schedule(FETCH_DATA_SERVICE_JOB_ID, this, FetchService::class.java)
        BaseService.schedule(SYNC_DATA_SERVICE_JOB_ID, this, SyncDataService::class.java)
        BaseService.schedule(FETCH_MEMBER_PHOTOS_SERVICE_JOB_ID, this, FetchMemberPhotosService::class.java)
        BaseService.schedule(SYNC_PHOTOS_SERVICE_JOB_ID, this, SyncPhotosService::class.java)
        BaseService.schedule(DELETE_SYNCED_PHOTOS_SERVICE_JOB_ID, this, DeleteSyncedPhotosService::class.java)
    }

    fun setSoftInputModeToResize() {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
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
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_switch_language -> {
                localeManager.toggleLocale(this)
                true
            }
            android.R.id.home -> {
                navigationManager.goBack()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
