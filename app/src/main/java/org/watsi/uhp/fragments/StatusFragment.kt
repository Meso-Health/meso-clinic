package org.watsi.uhp.fragments

import android.app.ProgressDialog
import android.arch.lifecycle.ViewModelProvider
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_status.fetch_billables_timestamp
import kotlinx.android.synthetic.main.fragment_status.fetch_diagnoses_timestamp
import kotlinx.android.synthetic.main.fragment_status.fetch_member_pictures_quantity
import kotlinx.android.synthetic.main.fragment_status.fetch_members_timestamp
import kotlinx.android.synthetic.main.fragment_status.refresh_button
import kotlinx.android.synthetic.main.fragment_status.sync_edited_members_quantity
import kotlinx.android.synthetic.main.fragment_status.sync_encounter_forms_quantity
import kotlinx.android.synthetic.main.fragment_status.sync_encounters_quantity
import kotlinx.android.synthetic.main.fragment_status.sync_id_events_quantity
import kotlinx.android.synthetic.main.fragment_status.sync_new_members_quantity
import kotlinx.android.synthetic.main.fragment_status.version_number
import org.watsi.device.managers.Logger
import org.watsi.device.managers.PreferencesManager

import org.watsi.domain.entities.Delta
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.MemberRepository
import org.watsi.uhp.R

import javax.inject.Inject

class StatusFragment : DaggerFragment() {

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var memberRepository: MemberRepository
    @Inject lateinit var deltaRepository: DeltaRepository
    @Inject lateinit var logger: Logger

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.version_and_sync_label)
        return inflater?.inflate(R.layout.fragment_status, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        try {
            val pInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)
            version_number.text = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            logger.error(e)
        }

        refreshValues()

        refresh_button.setOnClickListener {
            refreshValues()
        }
    }

    private fun updateTimestamps() {
        fetch_members_timestamp.text = preferencesManager.getMemberLastFetched().toString()
        fetch_billables_timestamp.text = preferencesManager.getBillablesLastFetched().toString()
        fetch_diagnoses_timestamp.text = preferencesManager.getDiagnosesLastFetched().toString()
    }

    private fun refreshValues() {
        updateTimestamps()

        val progressDialog = ProgressDialog(activity, ProgressDialog.STYLE_SPINNER)
        progressDialog.setCancelable(true)
        progressDialog.setMessage("Loading...")
        progressDialog.show()

        RefreshTask(progressDialog).execute()
    }

    private fun formattedQuantity(count: Int): String {
        return if (count == 0) {
            getString(R.string.all_synced)
        } else {
            count.toString() + " pending"
        }
    }

    // TODO: fix static warning
    inner class RefreshTask(private val progressDialog: ProgressDialog) :
            AsyncTask<String, Void, IntArray>() {
        override fun doInBackground(vararg params: String): IntArray {
            val counts = IntArray(6)
            counts[0] = 0
            // TODO: implement in background
//            counts[0] = memberRepository.membersWithPhotosToFetch().size
            val unsyncedMembers = deltaRepository.unsynced(Delta.ModelName.MEMBER).blockingGet()
            var newMembersCount = 0
            var editedMembersCount = 0
            for ((_, action) in unsyncedMembers) {
                if (action == Delta.Action.ADD) {
                    newMembersCount++
                } else {
                    editedMembersCount++
                }
            }
            counts[1] = newMembersCount
            counts[2] = editedMembersCount
            counts[3] = deltaRepository.unsynced(Delta.ModelName.IDENTIFICATION_EVENT).blockingGet().size
            counts[4] = deltaRepository.unsynced(Delta.ModelName.ENCOUNTER).blockingGet().size
            counts[5] = deltaRepository.unsynced(Delta.ModelName.ENCOUNTER_FORM).blockingGet().size
            return counts
        }

        override fun onPostExecute(result: IntArray) {
            fetch_member_pictures_quantity.text = formattedQuantity(result[0])
            sync_new_members_quantity.text = formattedQuantity(result[1])
            sync_edited_members_quantity.text = formattedQuantity(result[2])
            sync_id_events_quantity.text = formattedQuantity(result[3])
            sync_encounters_quantity.text = formattedQuantity(result[4])
            sync_encounter_forms_quantity.text = formattedQuantity(result[5])

            progressDialog.dismiss()
        }
    }
}
