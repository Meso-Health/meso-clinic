package org.watsi.uhp.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.ethiopia.fragment_household.member_list
import org.threeten.bp.Clock
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.adapters.MemberAdapter
import org.watsi.uhp.helpers.RecyclerViewHelper
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.HouseholdViewModel
import java.util.UUID
import javax.inject.Inject

class HouseholdFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var logger: Logger
    @Inject lateinit var clock: Clock

    lateinit var viewModel: HouseholdViewModel
    lateinit var memberAdapter: MemberAdapter

    companion object {
        const val PARAM_HOUSEHOLD_ID = "household_id"
        const val PARAM_SEARCH_METHOD = "search_method"

        fun forParams(householdId: UUID, searchMethod: IdentificationEvent.SearchMethod): HouseholdFragment {
            val householdFragment = HouseholdFragment()
            householdFragment.arguments = Bundle().apply {
                putSerializable(PARAM_HOUSEHOLD_ID, householdId)
                putSerializable(PARAM_SEARCH_METHOD, searchMethod)
            }
            return householdFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val householdId = arguments.getSerializable(PARAM_HOUSEHOLD_ID) as UUID
        val searchMethod = arguments.getSerializable(PARAM_SEARCH_METHOD) as IdentificationEvent.SearchMethod

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(HouseholdViewModel::class.java)
        viewModel.getObservable(householdId).observe(this, Observer {
            it?.let { viewState ->
                val members =
                    MemberWithIdEventAndThumbnailPhoto.asSortedListWithHeadOfHouseholdsFirst(
                        viewState.householdMembers
                    )

                memberAdapter.setMembers(members)
            }
        })

        memberAdapter = MemberAdapter(
            onItemSelect = { memberRelation: MemberWithIdEventAndThumbnailPhoto ->
                navigationManager.goTo(EditMemberFragment.forParams(
                    member = memberRelation.member,
                    searchMethod = searchMethod
                ))
            }
        )
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(context.getString(R.string.household_fragment_label), R.drawable.ic_arrow_back_white_24dp)
        return inflater?.inflate(R.layout.fragment_household, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        RecyclerViewHelper.setRecyclerView(member_list, memberAdapter, context, false)
    }
}
