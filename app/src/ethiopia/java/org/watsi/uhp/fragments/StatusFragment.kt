package org.watsi.uhp.fragments

import android.app.job.JobScheduler
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
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
import kotlinx.android.synthetic.ethiopia.fragment_status.fetch_billables_updated_at
import kotlinx.android.synthetic.ethiopia.fragment_status.fetch_diagnoses_updated_at
import kotlinx.android.synthetic.ethiopia.fragment_status.fetch_members_updated_at
import kotlinx.android.synthetic.ethiopia.fragment_status.unfetched_member_photos
import kotlinx.android.synthetic.ethiopia.fragment_status.unsynced_encounters
import kotlinx.android.synthetic.ethiopia.fragment_status.unsynced_identifications
import kotlinx.android.synthetic.ethiopia.fragment_status.unsynced_new_members
import org.watsi.device.managers.SessionManager
import org.watsi.uhp.BuildConfig
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.viewmodels.StatusViewModel
import javax.inject.Inject

class StatusFragment : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProviders.of(this, viewModelFactory).get(StatusViewModel::class.java)
        viewModel.getObservable().observe(this, Observer {
            it?.let { viewState ->
                viewState.membersUpdatedAt?.let {
                    fetch_members_updated_at.setValue(formattedUpdatedAt(it.toEpochMilli()))
                }
                viewState.billablesUpdatedAt?.let {
                    fetch_billables_updated_at.setValue(formattedUpdatedAt(it.toEpochMilli()))
                }
                viewState.diagnosesUpdatedAt?.let {
                    fetch_diagnoses_updated_at.setValue(formattedUpdatedAt(it.toEpochMilli()))
                }
                viewState.photosToFetchCount?.let {
                    unfetched_member_photos.setValue(formattedQuantity(it))
                }
                viewState.syncStatus.unsyncedNewMemberCount?.let {
                    unsynced_new_members.setValue(formattedQuantity(it))
                }
                viewState.syncStatus.unsyncedIdEventCount?.let {
                    unsynced_identifications.setValue(formattedQuantity(it))
                }
                viewState.syncStatus.unsyncedEncounterCount?.let {
                    unsynced_encounters.setValue(formattedQuantity(it))
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.version_and_sync_label)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_status, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        val username = sessionManager.currentToken()?.user?.username
        current_user.setValue(username)
        app_version.text = getString(R.string.app_version, BuildConfig.VERSION_NAME)
        android_version.text = getString(R.string.android_version, android.os.Build.VERSION.RELEASE)
    }

    private fun formattedQuantity(count: Int): String {
        return if (count == 0) {
            getString(R.string.all_synced)
        } else {
            "$count ${getString(R.string.waiting_to_sync)}"
        }
    }

    private fun formattedUpdatedAt(updatedAt: Long): String {
        return if (updatedAt == 0L) {
            getString(R.string.never_updated)
        } else {
            DateUtils.getRelativeTimeSpanString(updatedAt).toString()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.findItem(R.id.menu_sync_now).isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_sync_now -> {
                val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                jobScheduler.cancelAll()
                (activity as ClinicActivity).startServices()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
