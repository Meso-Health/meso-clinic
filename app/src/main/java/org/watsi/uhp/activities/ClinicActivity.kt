package org.watsi.uhp.activities

import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem

import net.hockeyapp.android.UpdateManager

import org.watsi.uhp.BuildConfig
import org.watsi.uhp.R
import org.watsi.uhp.helpers.ActivityHelper
import org.watsi.uhp.managers.MenuNavigationManager
import org.watsi.uhp.managers.SessionManager
import org.watsi.uhp.services.DeleteFetchedPhotoService
import org.watsi.uhp.services.DownloadMemberPhotosService
import org.watsi.uhp.services.FetchService
import org.watsi.uhp.services.SyncService

import dagger.android.support.DaggerAppCompatActivity
import org.watsi.uhp.fragments.CurrentPatientsFragment

import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.services.AbstractSyncJobService
import javax.inject.Inject

class ClinicActivity : DaggerAppCompatActivity() {

    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var navigationManager: NavigationManager

    companion object {
        private val FETCH_SERVICE_JOB_ID = 0
        private val SYNC_SERVICE_JOB_ID = 1
        private val DOWNLOAD_MEMBER_PHOTO_SERVICE_JOB_ID = 2
        private val DELETE_PHOTOS_SERVICE_JOB_ID = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clinic)

        ActivityHelper.setupBannerIfInTrainingMode(this)
        setupToolbar()
        startServices()

        navigationManager.goTo(CurrentPatientsFragment())
    }

    /**
     * This method gets called after onResume and will get called after both onCreate and
     * after onActivityResult which will ensure we force setUserAsLoggedIn when necessary
     */
    override fun onPostResume() {
        super.onPostResume()

        // TODO: launch AuthenticationActivity if not logged in
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
        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.schedule(AbstractSyncJobService.buildJobInfo(
                FETCH_SERVICE_JOB_ID, ComponentName(this, FetchService::class.java)))
        jobScheduler.schedule(AbstractSyncJobService.buildJobInfo(
                SYNC_SERVICE_JOB_ID, ComponentName(this, SyncService::class.java)))
        jobScheduler.schedule(AbstractSyncJobService.buildJobInfo(
                DOWNLOAD_MEMBER_PHOTO_SERVICE_JOB_ID,
                ComponentName(this, DownloadMemberPhotosService::class.java)))
        jobScheduler.schedule(AbstractSyncJobService.buildJobInfo(
                DELETE_PHOTOS_SERVICE_JOB_ID,
                ComponentName(this, DeleteFetchedPhotoService::class.java),
                false))
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.showOverflowMenu()
        setSupportActionBar(toolbar)
        toolbar.setOnMenuItemClickListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            val menuNavigationManager = MenuNavigationManager(
                    sessionManager, navigationManager, this)
            menuNavigationManager.handle(currentFragment.arguments, it)
        }
    }

    private fun checkForUpdates() {
        UpdateManager.register(this, BuildConfig.HOCKEYAPP_APP_ID)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onBackPressed() {
        navigationManager.goBack()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                navigationManager.goBack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
