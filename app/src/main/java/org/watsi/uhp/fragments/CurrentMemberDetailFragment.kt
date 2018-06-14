package org.watsi.uhp.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_current_member_detail.absentee_notification
import kotlinx.android.synthetic.main.fragment_current_member_detail.member_action_button
import kotlinx.android.synthetic.main.fragment_current_member_detail.member_detail
import kotlinx.android.synthetic.main.fragment_current_member_detail.replace_card_notification
import org.threeten.bp.Clock
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.entities.Member
import org.watsi.uhp.R
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.CurrentMemberDetailViewModel
import javax.inject.Inject

class CurrentMemberDetailFragment : DaggerFragment() {

    @Inject lateinit var clock: Clock
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var logger: Logger

    lateinit var member: Member
    lateinit var identificationEvent: IdentificationEvent
    lateinit var viewModel: CurrentMemberDetailViewModel

    companion object {
        const val PARAM_MEMBER = "member"
        const val PARAM_IDENTIFICAITON_EVENT = "identification_event"

        fun forMemberAndIdEvent(member: Member, identificationEvent: IdentificationEvent): CurrentMemberDetailFragment {
            val fragment = CurrentMemberDetailFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_MEMBER, member)
                putSerializable(PARAM_IDENTIFICAITON_EVENT, identificationEvent)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        member = arguments.getSerializable(PARAM_MEMBER) as Member
        identificationEvent = arguments.getSerializable(PARAM_IDENTIFICAITON_EVENT) as IdentificationEvent

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(CurrentMemberDetailViewModel::class.java)
        viewModel.getObservable(member, identificationEvent).observe(this, Observer {
            it?.member?.let { member ->
                this.member = member

                if (member.isAbsentee(clock)) {
                    absentee_notification.visibility = View.VISIBLE
                    absentee_notification.setOnActionClickListener {
                        navigationManager.goTo(EditMemberFragment.forMember(member.id))
                    }
                }

                if (member.cardId == null) {
                    replace_card_notification.visibility = View.VISIBLE
                    replace_card_notification.setOnClickListener {
                        navigationManager.goTo(EditMemberFragment.forMember(member.id))
                    }
                }

                member_detail.setMember(member, it.memberThumbnail, clock)
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.detail_fragment_label)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_current_member_detail, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        member_action_button.text = getString(R.string.detail_create_encounter)
        member_action_button.setOnClickListener {
            navigationManager.goTo(EncounterFragment.forEncounter(
                    viewModel.buildEncounter(identificationEvent)))
        }
        member_detail.setIdentificationEvent(identificationEvent)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        menu?.let {
            it.findItem(R.id.menu_member_edit).isVisible = true
            it.findItem(R.id.menu_enroll_newborn).isVisible = true
            it.findItem(R.id.menu_dismiss_member).isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_member_edit -> {
                navigationManager.goTo(EditMemberFragment.forMember(member.id))
            }
            R.id.menu_enroll_newborn -> {
                navigationManager.goTo(EnrollNewbornInfoFragment.forParent(member))
            }
            R.id.menu_dismiss_member -> {
                viewModel.dismiss(identificationEvent).subscribe({
                    navigationManager.popTo(CurrentPatientsFragment())
                }, {
                    logger.error(it)
                })
            }
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }
}
