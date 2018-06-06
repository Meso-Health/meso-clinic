package org.watsi.uhp.fragments

import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_current_patients.current_patients
import kotlinx.android.synthetic.main.fragment_current_patients.current_patients_label
import kotlinx.android.synthetic.main.fragment_current_patients.identification_button
import org.watsi.device.managers.Logger
import org.watsi.device.managers.SessionManager
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.activities.SearchByMemberCardActivity
import org.watsi.uhp.adapters.MemberAdapter
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.CurrentPatientsViewModel
import javax.inject.Inject

class CurrentPatientsFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var logger: Logger

    lateinit var viewModel: CurrentPatientsViewModel
    lateinit var memberAdapter: MemberAdapter

    companion object {
        const val SCAN_CARD_INTENT = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(CurrentPatientsViewModel::class.java)
        viewModel.getObservable().observe(this, Observer {
            it?.let { viewState ->
                val checkedInMembers = viewState.checkedInMembers
                if (checkedInMembers.isEmpty()) {
                    current_patients_label.visibility = View.GONE
                } else {
                    memberAdapter.setMembers(checkedInMembers)
                    current_patients_label.text = activity.resources.getQuantityString(
                            R.plurals.current_patients_label, checkedInMembers.size, checkedInMembers.size)
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.current_patients_fragment_label)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_current_patients, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        memberAdapter = MemberAdapter(
                showClinicNumber = true,
                showPhoneNumber = false,
                onItemSelect = { memberRelation: MemberWithIdEventAndThumbnailPhoto ->
                    if (memberRelation.identificationEvent != null) {
                        memberRelation.identificationEvent?.let {
                            navigationManager.goTo(CurrentMemberDetailFragment.forMemberAndIdEvent(
                                    memberRelation.member, it))
                        }
                    } else {
                        logger.error("Member shown on CurrentPatientsFragment has no corresponding " +
                                "IdentificationEvent", mapOf("memberId" to memberRelation.member.id.toString()))
                        navigationManager.goTo(CheckInMemberDetailFragment.forMember(memberRelation.member))
                    }
                })
        current_patients.adapter = memberAdapter
        current_patients.layoutManager = LinearLayoutManager(activity)
        current_patients.isNestedScrollingEnabled = false

        identification_button.setOnClickListener {
            startActivityForResult(Intent(activity, SearchByMemberCardActivity::class.java), SCAN_CARD_INTENT)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        menu!!.findItem(R.id.menu_logout).isVisible = true
        menu.findItem(R.id.menu_version).isVisible = true
        menu.findItem(R.id.menu_search_by_name_or_id).isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_version -> {
                navigationManager.goTo(StatusFragment())
            }
            R.id.menu_logout -> {
                AlertDialog.Builder(activity)
                        .setTitle(R.string.log_out_alert)
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes) { _, _ ->
                            sessionManager.logout()
                            (activity as ClinicActivity).navigateToAuthenticationActivity()
                        }.create().show()
            }
            R.id.menu_search_by_name_or_id -> {
                navigationManager.goTo(SearchMemberFragment())
            }
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val (member, error) = SearchByMemberCardActivity.parseResult(resultCode, data, logger)
        member?.let {
            navigationManager.goTo(CheckInMemberDetailFragment.forMember(it))
        }
        error?.let {
            // TODO: display error?
        }
    }
}
