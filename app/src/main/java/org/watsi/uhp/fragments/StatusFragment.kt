package org.watsi.uhp.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_status.*
import org.watsi.device.managers.Logger
import org.watsi.device.managers.PreferencesManager
import org.watsi.domain.repositories.MemberRepository
import org.watsi.uhp.R
import org.watsi.uhp.viewmodels.StatusViewModel
import javax.inject.Inject

class StatusFragment : DaggerFragment() {

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var memberRepository: MemberRepository
    @Inject lateinit var logger: Logger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProviders.of(this, viewModelFactory).get(StatusViewModel::class.java)
        viewModel.getObservable().observe(this, Observer {
            it?.let { viewState ->

            }
        })
    }
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.version_and_sync_label)
        return inflater?.inflate(R.layout.fragment_status, container, false)
    }

//    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
//        try {
//            val pInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)
//            version_number.text = pInfo.versionName
//        } catch (e: PackageManager.NameNotFoundException) {
//            logger.error(e)
//        }
//    }
//
//    private fun updateTimestamps() {
//        fetch_members_timestamp.text = preferencesManager.getMemberLastFetched().toString()
//        fetch_billables_timestamp.text = preferencesManager.getBillablesLastFetched().toString()
//        fetch_diagnoses_timestamp.text = preferencesManager.getDiagnosesLastFetched().toString()
//    }
//
//    private fun formattedQuantity(count: Int): String {
//        return if (count == 0) {
//            getString(R.string.all_synced)
//        } else {
//            count.toString() + " pending"
//        }
//    }
}
