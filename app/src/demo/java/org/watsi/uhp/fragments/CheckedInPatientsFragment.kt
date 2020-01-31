package org.watsi.uhp.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.demo.fragment_checked_in_patients.current_patients
import kotlinx.android.synthetic.demo.fragment_checked_in_patients.empty_container
import kotlinx.android.synthetic.demo.fragment_checked_in_patients.patients_container
import org.threeten.bp.Clock
import org.watsi.device.managers.Logger
import org.watsi.device.managers.SessionManager
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.adapters.MemberAdapter
import org.watsi.uhp.helpers.RecyclerViewHelper
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.CheckedInPatientsViewModel
import javax.inject.Inject

class CheckedInPatientsFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var logger: Logger
    @Inject lateinit var clock: Clock

    lateinit var viewModel: CheckedInPatientsViewModel
    lateinit var memberAdapter: MemberAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(CheckedInPatientsViewModel::class.java)

        viewModel.getMemberStateObservable().observe(this, Observer {
            it?.let { memberState ->
                val checkedInMembers = memberState.checkedInMembers
                if (checkedInMembers.isEmpty()) {
                    empty_container.visibility = View.VISIBLE
                    patients_container.visibility = View.GONE
                } else {
                    empty_container.visibility = View.GONE
                    patients_container.visibility = View.VISIBLE
                    memberAdapter.setMembers(checkedInMembers)
                }
            }
        })

        memberAdapter = MemberAdapter(
            onItemSelect = { memberRelation: MemberWithIdEventAndThumbnailPhoto ->
                memberRelation.identificationEvent?.id?.let { identificationEventId ->
                    navigationManager.goTo(EditMemberFragment.forClaimsPreparation(
                        member = memberRelation.member,
                        identificationEventId = identificationEventId
                    ))
                } ?: run {
                    throw IllegalStateException("identificationEventId is null for checked-in member")
                }
            }
        )
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(context.getString(R.string.checked_in_patients_fragment_label), null)
        return inflater?.inflate(R.layout.fragment_checked_in_patients, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        RecyclerViewHelper.setRecyclerView(current_patients, memberAdapter, context, false)
    }
}
