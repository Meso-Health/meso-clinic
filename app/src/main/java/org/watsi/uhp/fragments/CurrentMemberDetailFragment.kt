package org.watsi.uhp.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_member_detail.absentee_notification
import kotlinx.android.synthetic.main.fragment_member_detail.member_action_button
import kotlinx.android.synthetic.main.fragment_member_detail.member_age_and_gender
import kotlinx.android.synthetic.main.fragment_member_detail.member_card_id_detail_fragment
import kotlinx.android.synthetic.main.fragment_member_detail.member_name_detail_fragment
import kotlinx.android.synthetic.main.fragment_member_detail.member_phone_number
import kotlinx.android.synthetic.main.fragment_member_detail.member_photo
import kotlinx.android.synthetic.main.fragment_member_detail.replace_card_notification
import org.threeten.bp.Clock
import org.watsi.domain.entities.IdentificationEvent

import org.watsi.domain.repositories.PhotoRepository
import org.watsi.uhp.R
import org.watsi.uhp.helpers.PhotoLoaderHelper
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.CurrentMemberDetailViewModel
import javax.inject.Inject

class CurrentMemberDetailFragment : DaggerFragment() {

    @Inject lateinit var clock: Clock
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var photoRepository: PhotoRepository

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

        val viewModel = ViewModelProviders.of(this, viewModelFactory).get(CurrentMemberDetailViewModel::class.java)
        viewModel.getObservable(identificationEvent.memberId).observe(this, Observer {
            it?.let { viewState ->
                val member = viewState.member
                if (member.isAbsentee()) {
                    absentee_notification.visibility = View.VISIBLE
                    absentee_notification.setOnActionClickListener {
                        navigationManager.goTo(EnrollmentMemberPhotoFragment.forMember(member))
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
                PhotoLoaderHelper(activity, photoRepository).loadMemberPhoto(
                        member, member_photo, R.dimen.detail_fragment_photo_width, R.dimen.detail_fragment_photo_height)
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
        menu!!.findItem(R.id.menu_dismiss_member).isVisible = true
        // TODO: need to add listener/logic for dismiss member
    }
}
