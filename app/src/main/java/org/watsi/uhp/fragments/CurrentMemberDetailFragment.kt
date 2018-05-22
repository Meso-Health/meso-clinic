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
import android.widget.ArrayAdapter
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_member_detail.absentee_notification
import kotlinx.android.synthetic.main.fragment_member_detail.household_members
import kotlinx.android.synthetic.main.fragment_member_detail.member_action_button
import kotlinx.android.synthetic.main.fragment_member_detail.member_age_and_gender
import kotlinx.android.synthetic.main.fragment_member_detail.member_card_id_detail_fragment
import kotlinx.android.synthetic.main.fragment_member_detail.member_name_detail_fragment
import kotlinx.android.synthetic.main.fragment_member_detail.member_phone_number
import kotlinx.android.synthetic.main.fragment_member_detail.member_photo
import kotlinx.android.synthetic.main.fragment_member_detail.replace_card_notification
import org.threeten.bp.Clock
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.entities.Member
import org.watsi.uhp.R
import org.watsi.uhp.helpers.PhotoLoader
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.CurrentMemberDetailViewModel
import javax.inject.Inject

class CurrentMemberDetailFragment : DaggerFragment() {

    @Inject lateinit var clock: Clock
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

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
        viewModel.getObservable(member).observe(this, Observer {
            it?.member?.let { member ->
                this.member = member

                if (member.isAbsentee()) {
                    absentee_notification.visibility = View.VISIBLE
                    absentee_notification.setOnActionClickListener {
                        navigationManager.goTo(CompleteEnrollmentFragment.forMember(member.id))
                    }
                }

                if (member.cardId == null) {
                    replace_card_notification.visibility = View.VISIBLE
                    replace_card_notification.setOnClickListener {
                        navigationManager.goTo(MemberEditFragment.forMember(member))
                    }
                }

                member_name_detail_fragment.text = member.name
                member_age_and_gender.text = "${member.getAgeYears(clock)} - ${member.gender}"
                member_card_id_detail_fragment.text = member.cardId
                member_phone_number.text = member.phoneNumber
            }
            
            it?.householdMembers?.let { householdMembers ->
                // TODO: Make this take a list of MemberWithThumbnail.
                household_members.adapter = ArrayAdapter<Member>(context, android.R.layout.simple_list_item_1, householdMembers.map { it.member })
            }

            it?.memberThumbnail?.bytes?.let { bytes ->
                PhotoLoader.loadMemberPhoto(bytes, member_photo, context)
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.detail_fragment_label)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_member_detail, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        member_action_button.text = getString(R.string.detail_create_encounter)
        member_action_button.setOnClickListener {
            navigationManager.goTo(EncounterFragment.forIdentificationEvent(identificationEvent))
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        menu!!.findItem(R.id.menu_member_edit).isVisible = true
        menu.findItem(R.id.menu_enroll_newborn).isVisible = true
        menu.findItem(R.id.menu_dismiss_member).isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_member_edit -> {
                navigationManager.goTo(MemberEditFragment.forMember(member))
            }
            R.id.menu_enroll_newborn -> {
                navigationManager.goTo(EnrollNewbornInfoFragment.forParent(member))
            }
            R.id.menu_dismiss_member -> {
                viewModel.dismiss(identificationEvent).subscribe({
                    navigationManager.popTo(CurrentPatientsFragment())
                }, {
                    // TODO: handle error
                })
            }
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }
}
