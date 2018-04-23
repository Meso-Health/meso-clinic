package org.watsi.uhp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_member_detail.absentee_notification
import kotlinx.android.synthetic.main.fragment_member_detail.member_action_button
import kotlinx.android.synthetic.main.fragment_member_detail.replace_card_notification
import org.watsi.domain.entities.IdentificationEvent

import org.watsi.domain.entities.Member
import org.watsi.domain.repositories.IdentificationEventRepository
import org.watsi.domain.repositories.MemberRepository
import org.watsi.uhp.R
import org.watsi.uhp.managers.NavigationManager
import javax.inject.Inject

class CurrentMemberDetailFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var identificationEventRepository: IdentificationEventRepository
    @Inject lateinit var memberRepository: MemberRepository

    lateinit var member: Member
    lateinit var identificationEvent: IdentificationEvent

    companion object {
        const val PARAM_IDENTIFICATION_EVENT = "identification_event"

        fun forIdentificationEvent(idEvent: IdentificationEvent): CurrentMemberDetailFragment {
            val fragment = CurrentMemberDetailFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_IDENTIFICATION_EVENT, idEvent)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        identificationEvent = arguments.getSerializable(PARAM_IDENTIFICATION_EVENT) as IdentificationEvent
        // TODO: don't query on main thread
        member = memberRepository.find(identificationEvent.memberId)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.detail_fragment_label)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_member_detail, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        if (member.isAbsentee()) {
            absentee_notification.visibility = View.VISIBLE
            absentee_notification.setOnActionClickListener {
                // TODO: navigate to complete enrollment fragment
            }
        }
        if (member.cardId == null) {
            replace_card_notification.visibility = View.VISIBLE
            replace_card_notification.setOnClickListener {
                navigationManager.goTo(MemberEditFragment.forMember(member))
            }
        }

        // TODO: need to set fragment views with member information

        member_action_button.text = getString(R.string.detail_create_encounter)
        member_action_button.setOnClickListener {
            // TODO: navigate to EncounterFragment (or PresentingCondition if we are still doing that)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        menu!!.findItem(R.id.menu_dismiss_member).isVisible = true
        // TODO: need to add listener/logic for dismiss member
    }
}
