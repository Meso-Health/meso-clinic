package org.watsi.uhp.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.ethiopia.fragment_status.fetch_billables_updated_at
import kotlinx.android.synthetic.ethiopia.fragment_status.fetch_diagnoses_updated_at
import kotlinx.android.synthetic.ethiopia.fragment_status.unsynced_encounters
import kotlinx.android.synthetic.ethiopia.fragment_status.unsynced_new_members
import kotlinx.android.synthetic.ethiopia.fragment_status.version
import org.watsi.device.managers.Logger
import org.watsi.domain.repositories.MemberRepository
import org.watsi.uhp.R
import org.watsi.uhp.viewmodels.StatusViewModel
import javax.inject.Inject

class StatusFragment : DaggerFragment() {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var memberRepository: MemberRepository
    @Inject lateinit var logger: Logger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProviders.of(this, viewModelFactory).get(StatusViewModel::class.java)
        viewModel.getObservable().observe(this, Observer {
            it?.let { viewState ->
                viewState.billablesUpdatedAt?.let {
                    fetch_billables_updated_at.setValue(formattedUpdatedAt(it.toEpochMilli()))
                }
                viewState.diagnosesUpdatedAt?.let {
                    fetch_diagnoses_updated_at.setValue(formattedUpdatedAt(it.toEpochMilli()))
                }
                viewState.syncStatus.unsyncedNewMemberCount?.let {
                    unsynced_new_members.setValue(formattedQuantity(it))
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
        try {
            val pInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)
            version.setValue(pInfo.versionName)
        } catch (e: PackageManager.NameNotFoundException) {
            logger.error(e)
        }
    }

    private fun formattedQuantity(count: Int): String {
        return if (count == 0) {
            getString(R.string.all_synced)
        } else {
            count.toString() + " pending"
        }
    }

    private fun formattedUpdatedAt(updatedAt: Long): String {
        return if (updatedAt == 0L) {
            getString(R.string.waiting_to_sync)
        } else {
            DateUtils.getRelativeTimeSpanString(updatedAt).toString()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        val switchLanguageItem = menu.findItem(R.id.menu_switch_language)
        switchLanguageItem.isVisible = true
    }
}
