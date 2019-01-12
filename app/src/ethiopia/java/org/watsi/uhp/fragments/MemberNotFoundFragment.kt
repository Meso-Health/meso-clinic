package org.watsi.uhp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.ethiopia.fragment_member_not_found.enter_claim_button
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.managers.NavigationManager
import javax.inject.Inject

class MemberNotFoundFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager

    companion object {
        const val PARAM_MEMBERSHIP_NUMBER = "membership_number"

        fun forMembershipNumber(membershipNumber: String): MemberNotFoundFragment {
            val memberNotFoundFragment = MemberNotFoundFragment()
            memberNotFoundFragment.arguments = Bundle().apply {
                putString(PARAM_MEMBERSHIP_NUMBER, membershipNumber)
            }
            return memberNotFoundFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(
            getString(R.string.member_not_found_fragment_label), R.drawable.ic_arrow_back_white_24dp)
        return inflater?.inflate(R.layout.fragment_member_not_found, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        enter_claim_button.setOnClickListener {
            navigationManager.goTo(MemberInformationFragment.withMembershipNumber(
                arguments.getString(PARAM_MEMBERSHIP_NUMBER)))
        }
    }
}
