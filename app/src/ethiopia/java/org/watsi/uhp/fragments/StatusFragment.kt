package org.watsi.uhp.fragments

import android.app.AlertDialog
import android.app.job.JobScheduler
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.ethiopia.fragment_status.android_version
import kotlinx.android.synthetic.ethiopia.fragment_status.app_version
import kotlinx.android.synthetic.ethiopia.fragment_status.current_user
import kotlinx.android.synthetic.ethiopia.fragment_status.provider_type
import kotlinx.android.synthetic.ethiopia.fragment_status.data_last_fetched_at
import kotlinx.android.synthetic.ethiopia.fragment_status.data_last_synced_at
import kotlinx.android.synthetic.ethiopia.fragment_status.fetch_data_error
import kotlinx.android.synthetic.ethiopia.fragment_status.fetch_data_progress_bar
import kotlinx.android.synthetic.ethiopia.fragment_status.fetch_photos_container
import kotlinx.android.synthetic.ethiopia.fragment_status.fetch_photos_error
import kotlinx.android.synthetic.ethiopia.fragment_status.fetch_photos_progress_bar
import kotlinx.android.synthetic.ethiopia.fragment_status.last_fetched_billables
import kotlinx.android.synthetic.ethiopia.fragment_status.last_fetched_diagnoses
import kotlinx.android.synthetic.ethiopia.fragment_status.last_fetched_member_photos
import kotlinx.android.synthetic.ethiopia.fragment_status.last_fetched_members
import kotlinx.android.synthetic.ethiopia.fragment_status.last_fetched_returned_claims
import kotlinx.android.synthetic.ethiopia.fragment_status.photos_last_fetched_at
import kotlinx.android.synthetic.ethiopia.fragment_status.photos_last_synced_at
import kotlinx.android.synthetic.ethiopia.fragment_status.sync_data_error
import kotlinx.android.synthetic.ethiopia.fragment_status.sync_data_progress_bar
import kotlinx.android.synthetic.ethiopia.fragment_status.sync_photos_error
import kotlinx.android.synthetic.ethiopia.fragment_status.sync_photos_progress_bar
import kotlinx.android.synthetic.ethiopia.fragment_status.unsynced_encounters
import kotlinx.android.synthetic.ethiopia.fragment_status.unsynced_identification_events
import kotlinx.android.synthetic.ethiopia.fragment_status.unsynced_member_photos
import kotlinx.android.synthetic.ethiopia.fragment_status.unsynced_members
import kotlinx.android.synthetic.ethiopia.fragment_status.unsynced_price_schedules
import org.watsi.device.managers.Logger
import org.watsi.device.managers.NetworkManager
import org.watsi.device.managers.SessionManager
import org.watsi.uhp.BuildConfig
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.helpers.EnumHelper
import org.watsi.uhp.services.BaseService
import org.watsi.uhp.services.FetchDataService
import org.watsi.uhp.services.FetchPhotosService
import org.watsi.uhp.services.SyncDataService
import org.watsi.uhp.services.SyncPhotosService
import org.watsi.uhp.viewmodels.StatusViewModel
import javax.inject.Inject

class StatusFragment : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var networkManager: NetworkManager
    @Inject lateinit var logger: Logger
    lateinit var viewModel: StatusViewModel
    private lateinit var broadcastReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerReceiver()

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(StatusViewModel::class.java)

        viewModel.getNetworkObservable().observe(this, Observer {
            it?.let { viewState ->
                refreshNetworkIndicators(viewState.isDataFetching, viewState.dataFetchErrors, fetch_data_progress_bar, fetch_data_error)
                refreshNetworkIndicators(viewState.isPhotoFetching, viewState.photoFetchErrors, fetch_photos_progress_bar, fetch_photos_error)
                refreshNetworkIndicators(viewState.isDataSyncing, viewState.dataSyncErrors, sync_data_progress_bar, sync_data_error)
                refreshNetworkIndicators(viewState.isPhotoSyncing, viewState.photoSyncErrors, sync_photos_progress_bar, sync_photos_error)
            }
        })

        viewModel.getObservable().observe(this, Observer {
            it?.let { viewState ->
                data_last_fetched_at.setValue(formattedUpdatedAt(viewState.dataFetchedAt.toEpochMilli()))
                photos_last_fetched_at.setValue(formattedUpdatedAt(viewState.photoFetchedAt.toEpochMilli()))
                data_last_synced_at.setValue(formattedUpdatedAt(viewState.dataSyncedAt.toEpochMilli()))
                photos_last_synced_at.setValue(formattedUpdatedAt(viewState.photoSyncedAt.toEpochMilli()))

                last_fetched_members.setValue(formattedUpdatedAt(viewState.membersFetchedAt.toEpochMilli()))
                last_fetched_billables.setValue(formattedUpdatedAt(viewState.billablesFetchedAt.toEpochMilli()))
                last_fetched_diagnoses.setValue(formattedUpdatedAt(viewState.diagnosesFetchedAt.toEpochMilli()))
                last_fetched_returned_claims.setValue(formattedUpdatedAt(viewState.returnedClaimsFetchedAt.toEpochMilli()))
                if (viewState.photosToFetchCount == 0) {
                    last_fetched_member_photos.setValue(formattedUpdatedAt(viewState.memberPhotosFetchedAt.toEpochMilli()))
                } else {
                    last_fetched_member_photos.setValue(formattedFetchQuantity(viewState.photosToFetchCount))
                }

                unsynced_members.setValue(formattedSyncQuantity(viewState.syncStatus.unsyncedMembersCount))
                unsynced_identification_events.setValue(formattedSyncQuantity(viewState.syncStatus.unsyncedIdEventsCount))
                unsynced_price_schedules.setValue(formattedSyncQuantity(viewState.syncStatus.unsyncedPriceSchedulesCount))
                unsynced_encounters.setValue(formattedSyncQuantity(viewState.syncStatus.unsyncedEncountersCount))
                unsynced_member_photos.setValue(formattedSyncQuantity(viewState.syncStatus.unsyncedPhotosCount))
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.status_fragment_label)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_status, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        fetch_photos_container.visibility = getVisibilityFromPermission(SessionManager.Permissions.FETCH_PHOTOS)
        last_fetched_billables.visibility = getVisibilityFromPermission(SessionManager.Permissions.FETCH_BILLABLES)
        last_fetched_diagnoses.visibility = getVisibilityFromPermission(SessionManager.Permissions.FETCH_DIAGNOSES)
        last_fetched_returned_claims.visibility = getVisibilityFromPermission(SessionManager.Permissions.FETCH_RETURNED_CLAIMS)
        unsynced_price_schedules.visibility = getVisibilityFromPermission(SessionManager.Permissions.SYNC_PRICE_SCHEDULES)

        val username = sessionManager.currentAuthenticationToken()?.user?.username
        val providerType = sessionManager.currentAuthenticationToken()?.user?.providerType
        current_user.setValue(username)
        provider_type.setValue(providerType?.let {EnumHelper.providerTypeToDisplayedString(it, context, logger)})
        app_version.text = getString(R.string.app_version, BuildConfig.VERSION_NAME)
        android_version.text = getString(R.string.android_version, android.os.Build.VERSION.RELEASE)
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.findItem(R.id.menu_sync_now).isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_sync_now -> {
                val requireNetwork = BuildConfig.BUILD_TYPE != "debug"

                if (requireNetwork && !networkManager.isNetworkAvailable()) {
                    AlertDialog.Builder(activity)
                            .setTitle(getString(R.string.no_internet_title))
                            .setMessage(getString(R.string.no_internet_prompt))
                            .setPositiveButton(R.string.ok) { _, _ ->
                                // no-op
                            }
                            .create().show()
                } else {
                    // explicitly toggle spinners when user presses "sync now" button to avoid
                    // visual delay before spinners are toggled from job services actually being kicked off
                    viewModel.updateFetchDataStatus(true)
                    viewModel.updateFetchPhotosStatus(true)
                    viewModel.updateSyncDataStatus(true)
                    viewModel.updateSyncPhotosStatus(true)
                    val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                    jobScheduler.cancelAll()
                    (activity as ClinicActivity).startServices()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getVisibilityFromPermission(permission: SessionManager.Permissions): Int {
        return if (sessionManager.userHasPermission(permission)) { View.VISIBLE } else { View.GONE }
    }

    private fun registerReceiver() {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val className = intent.getStringExtra(BaseService.PARAM_SERVICE_CLASS)
                val isRunning = intent.getBooleanExtra(BaseService.PARAM_IS_RUNNING, false)
                val errors = intent.getStringArrayListExtra(BaseService.PARAM_ERRORS).orEmpty()

                when (className) {
                    FetchDataService::class.java.toString() -> viewModel.updateFetchDataStatus(isRunning, errors)
                    FetchPhotosService::class.java.toString() -> viewModel.updateFetchPhotosStatus(isRunning, errors)
                    SyncDataService::class.java.toString() -> viewModel.updateSyncDataStatus(isRunning, errors)
                    SyncPhotosService::class.java.toString() -> viewModel.updateSyncPhotosStatus(isRunning, errors)
                }
            }
        }
        context.registerReceiver(broadcastReceiver, IntentFilter(BaseService.ACTION_SERVICE_UPDATE))
    }

    private fun refreshNetworkIndicators(isRunning: Boolean, errorMessages: List<String>, progressBar: View, errorIcon: View) {
        if (isRunning) {
            progressBar.visibility = View.VISIBLE
            errorIcon.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE

            if (errorMessages.isNotEmpty()) {
                errorIcon.setOnClickListener {
                    AlertDialog.Builder(activity).setMessage(errorMessages.joinToString("\n\n")).create().show()
                }
                errorIcon.visibility = View.VISIBLE
            } else {
                errorIcon.visibility = View.GONE
            }
        }
    }

    private fun formattedUpdatedAt(updatedAt: Long): String {
        return if (updatedAt == 0L) {
            getString(R.string.never)
        } else {
            DateUtils.getRelativeTimeSpanString(updatedAt).toString()
        }
    }

    private fun formattedSyncQuantity(count: Int): String {
        return if (count == 0) {
            getString(R.string.all_synced)
        } else {
            "$count ${getString(R.string.waiting_to_sync)}"
        }
    }

    private fun formattedFetchQuantity(count: Int): String {
        return if (count == 0) {
            getString(R.string.all_fetched)
        } else {
            "$count ${getString(R.string.waiting_to_fetch)}"
        }
    }
}
