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
import io.reactivex.android.schedulers.AndroidSchedulers
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
import org.watsi.domain.relations.MemberWithThumbnail
import org.watsi.domain.repositories.PhotoRepository
import org.watsi.uhp.R
import org.watsi.uhp.helpers.PhotoLoader
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.CurrentMemberDetailViewModel
import javax.inject.Inject

class CurrentMemberDetailFragment : DaggerFragment() {

    @Inject lateinit var clock: Clock
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var photoRepository: PhotoRepository

    lateinit var identificationEvent: IdentificationEvent
    lateinit var viewModel: CurrentMemberDetailViewModel
    private var memberWithThumbnail: MemberWithThumbnail? = null

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

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(CurrentMemberDetailViewModel::class.java)
        viewModel.getObservable(identificationEvent.memberId).observe(this, Observer {
            it?.let { viewState ->
                val memberWithThumbnail = viewState.memberWIthThumbnail
                this.memberWithThumbnail = memberWithThumbnail

                val member = memberWithThumbnail.member
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
                member.thumbnailPhotoId?.let { photoId ->
                    photoRepository.find(photoId)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ photo ->
                                PhotoLoader.loadMemberPhoto(photo.bytes, member_photo, activity)
                            }, {
                                // TODO: handle error
                            })
                }
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
                memberWithThumbnail?.member?.let {
                    navigationManager.goTo(MemberEditFragment.forMember(it))
                }
            }
            R.id.menu_enroll_newborn -> {
                memberWithThumbnail?.member?.let {
                    navigationManager.goTo(EnrollNewbornInfoFragment.forParent(it))
                }
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
