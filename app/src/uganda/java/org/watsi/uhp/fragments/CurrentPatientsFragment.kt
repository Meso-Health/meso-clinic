package org.watsi.uhp.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.job.JobScheduler
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.uganda.fragment_current_patients.current_patients
import kotlinx.android.synthetic.uganda.fragment_current_patients.identification_button
import kotlinx.android.synthetic.uganda.fragment_current_patients.update_notification
import org.threeten.bp.Clock
import org.watsi.device.managers.Logger
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.IdentificationEvent.SearchMethod
import org.watsi.domain.entities.Member
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.activities.SearchByMemberCardActivity
import org.watsi.uhp.adapters.MemberAdapter
import org.watsi.uhp.helpers.RecyclerViewHelper
import org.watsi.uhp.helpers.SnackbarHelper
import org.watsi.uhp.managers.AppUpdateManager
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.CurrentPatientsViewModel
import javax.inject.Inject

class CurrentPatientsFragment : DaggerFragment() {
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var logger: Logger
    @Inject lateinit var clock: Clock
    @Inject lateinit var deltaRepository: DeltaRepository
    private lateinit var appUpdateManager: AppUpdateManager

    lateinit var viewModel: CurrentPatientsViewModel
    lateinit var memberAdapter: MemberAdapter

    private var snackbarMessageToShow: String? = null

    companion object {
        const val SCAN_CARD_INTENT = 1
        const val PARAM_SNACKBAR_MESSAGE = "member"

        fun withSnackbarMessage(message: String): CurrentPatientsFragment {
            val fragment = CurrentPatientsFragment()
            fragment.arguments = Bundle().apply {
                putString(PARAM_SNACKBAR_MESSAGE, message)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        snackbarMessageToShow = arguments?.getString(PARAM_SNACKBAR_MESSAGE)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(CurrentPatientsViewModel::class.java)
        viewModel.getObservable().observe(this, Observer {
            it?.let { viewState ->
                val checkedInMembers = viewState.checkedInMembers
                memberAdapter.setMembers(checkedInMembers)
            }
        })

        memberAdapter = MemberAdapter(
                onItemSelect = { memberRelation: MemberWithIdEventAndThumbnailPhoto ->
                    if (memberRelation.identificationEvent != null) {
                        memberRelation.identificationEvent?.let {
                            navigationManager.goTo(CurrentMemberDetailFragment.forMemberAndIdEvent(
                                    memberRelation.member, it))
                        }
                    } else {
                        logger.error("Member shown on CurrentPatientsFragment has no corresponding " +
                                "IdentificationEvent", mapOf("memberId" to memberRelation.member.id.toString()))
                    }
                }
        )
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(context.getString(R.string.current_patients_fragment_label), null)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_current_patients, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        RecyclerViewHelper.setRecyclerView(current_patients, memberAdapter, context, false)

        identification_button.setOnClickListener {
            startActivityForResult(Intent(activity, SearchByMemberCardActivity::class.java), SCAN_CARD_INTENT)
        }

        snackbarMessageToShow?.let { snackbarMessage ->
            SnackbarHelper.show(identification_button, context, snackbarMessage)
            snackbarMessageToShow = null
        }
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager = AppUpdateManager(
            activity = activity,
            logger = logger
        )
        appUpdateManager.setOnUpdateAvailable { appUpdateInfo ->
            // If there is an update, show the update notification bar and set what happens when it is clicked.
            update_notification.visibility = View.VISIBLE
            update_notification.setOnClickListener {
                deltaRepository.unsyncedCount().observeOn(AndroidSchedulers.mainThread()).subscribe(
                    { unsyncedCount ->
                        if (unsyncedCount == 0) {
                            // Go ahead and move forward with the update without a warning message.
                            appUpdateManager.requestUpdate(appUpdateInfo)
                        } else {
                            // Show an error message before moving forward with the update.
                            AlertDialog.Builder(activity)
                                    .setTitle(R.string.update_alert_dialog_title)
                                    .setMessage(R.string.update_alert_dialog_message)
                                    .setNegativeButton(android.R.string.no, null)
                                    .setPositiveButton(R.string.yes_update_anyway) { _, _ ->
                                        appUpdateManager.requestUpdate(appUpdateInfo)
                                    }.create().show()
                        }
                    },
                    { error ->
                        logger.error(error)
                    }
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        appUpdateManager.tearDown()
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        menu?.let {
            it.findItem(R.id.menu_logout).isVisible = true
            it.findItem(R.id.menu_status).isVisible = true
            it.findItem(R.id.menu_settings).isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_status -> {
                navigationManager.goTo(StatusFragment())
            }
            R.id.menu_settings -> {
                navigationManager.goTo(SettingsFragment())
            }
            R.id.menu_logout -> {
                AlertDialog.Builder(activity)
                        .setTitle(R.string.log_out_alert)
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes) { _, _ ->
                            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                            jobScheduler.cancelAll()
                            sessionManager.logout()
                            (activity as ClinicActivity).navigateToAuthenticationActivity()
                        }.create().show()
            }
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        return when (resultCode) {
            Activity.RESULT_OK -> {
                val member = data?.getSerializableExtra(SearchByMemberCardActivity.MEMBER_RESULT_KEY) as Member?
                if (member != null) {
                    navigationManager.goTo(CheckInMemberDetailFragment.forMemberWithSearchMethod(
                            member,SearchMethod.SCAN_BARCODE))
                } else {
                    logger.error("QRCodeActivity returned null member with resultCode: Activity.RESULT_OK")
                }
            }
            SearchByMemberCardActivity.RESULT_REDIRECT_TO_SEARCH_FRAGMENT -> {
                navigationManager.goTo(MemberSearchFragment())
            }
            Activity.RESULT_CANCELED -> { }
            else -> {
                logger.error("QrCodeActivity.parseResult called with resultCode: $resultCode")
            }
        }
    }
}
