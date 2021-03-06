package org.watsi.uhp.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import dagger.android.support.DaggerFragment
import io.reactivex.Completable
import kotlinx.android.synthetic.uganda.fragment_checkin_member_detail.absentee_notification
import kotlinx.android.synthetic.uganda.fragment_checkin_member_detail.household_list_empty_state
import kotlinx.android.synthetic.uganda.fragment_checkin_member_detail.household_members_list
import kotlinx.android.synthetic.uganda.fragment_checkin_member_detail.household_panel_summary
import kotlinx.android.synthetic.uganda.fragment_checkin_member_detail.member_action_button
import kotlinx.android.synthetic.uganda.fragment_checkin_member_detail.member_detail
import kotlinx.android.synthetic.uganda.fragment_checkin_member_detail.notification_container
import kotlinx.android.synthetic.uganda.fragment_checkin_member_detail.replace_card_notification
import org.threeten.bp.Clock
import org.watsi.device.managers.Logger
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.entities.IdentificationEvent.SearchMethod
import org.watsi.domain.entities.Member
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.domain.usecases.CreateIdentificationEventUseCase
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.adapters.MemberAdapter
import org.watsi.uhp.helpers.LayoutHelper
import org.watsi.uhp.helpers.RecyclerViewHelper
import org.watsi.uhp.managers.KeyboardManager
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.CheckInMemberDetailViewModel
import java.io.Serializable
import java.util.UUID
import javax.inject.Inject

class CheckInMemberDetailFragment : DaggerFragment() {

    @Inject lateinit var clock: Clock
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var createIdentificationEventUseCase: CreateIdentificationEventUseCase
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var logger: Logger
    @Inject lateinit var keyboardManager: KeyboardManager

    lateinit var viewModel: CheckInMemberDetailViewModel
    lateinit var member: Member
    lateinit var memberAdapter: MemberAdapter
    lateinit var searchFields: SearchFields

    data class SearchFields(val searchMethod: SearchMethod, var throughMemberId: UUID? = null) : Serializable

    companion object {
        const val PARAM_MEMBER = "member"
        const val PARAM_SEARCH_FIELDS = "search_fields"

        fun forMemberWithSearchMethod(member: Member, searchMethod: SearchMethod): CheckInMemberDetailFragment {
            return forMemberWithSearchFields(member, SearchFields(searchMethod))
        }

        private fun forMemberWithSearchFields(member: Member, searchFields: SearchFields): CheckInMemberDetailFragment{
            val fragment = CheckInMemberDetailFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_MEMBER, member)
                putSerializable(PARAM_SEARCH_FIELDS, searchFields)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        member = arguments.getSerializable(PARAM_MEMBER) as Member
        searchFields = arguments.getSerializable(PARAM_SEARCH_FIELDS) as SearchFields

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(CheckInMemberDetailViewModel::class.java)
        viewModel.getObservable(member).observe(this, Observer {
            it?.member?.let { member ->
                this.member = member

                if (member.isAbsentee() || member.cardId == null) {
                    notification_container.visibility = View.VISIBLE

                    if (member.isAbsentee()) {
                        absentee_notification.visibility = View.VISIBLE
                    } else {
                        absentee_notification.visibility = View.GONE
                    }
                    absentee_notification.setOnClickListener {
                        navigationManager.goTo(EditMemberFragment.forMember(member.id))
                    }

                    if (member.cardId == null) {
                        replace_card_notification.visibility = View.VISIBLE
                    } else {
                        replace_card_notification.visibility = View.GONE
                    }
                    replace_card_notification.setOnClickListener {
                        navigationManager.goTo(EditMemberFragment.forMember(member.id))
                    }
                }

                member_detail.setMember(member, it.memberThumbnail, clock)
            }

            it?.isMemberCheckedIn?.let { isMemberCheckedIn ->
                member_action_button.visibility = View.VISIBLE

                if (isMemberCheckedIn) {
                    member_action_button.isEnabled = false
                    member_action_button.text = getString(R.string.checked_in)
                } else {
                    member_action_button.isEnabled = true
                    member_action_button.text = getString(R.string.check_in)
                    member_action_button.setOnClickListener {
                        launchClinicNumberDialog()
                    }
                }
            }

            it?.householdMembers?.let { householdMembers ->
                memberAdapter.setMembers(householdMembers)
                household_panel_summary.text = context.resources.getQuantityString(
                        R.plurals.household_members_label, householdMembers.size, householdMembers.size)
                if (householdMembers.isEmpty()) {
                    household_list_empty_state.visibility = View.VISIBLE
                } else {
                    household_list_empty_state.visibility = View.GONE
                }
            }
        })

        memberAdapter = MemberAdapter(
                onItemSelect = { memberRelation: MemberWithIdEventAndThumbnailPhoto ->
                    val throughMemberId = searchFields.throughMemberId ?: memberRelation.member.id
                    navigationManager.goTo(CheckInMemberDetailFragment.forMemberWithSearchFields(
                            memberRelation.member,
                            SearchFields(searchFields.searchMethod, throughMemberId)))
                }
        )
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(context.getString(R.string.detail_fragment_label), R.drawable.ic_arrow_back_white_24dp)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_checkin_member_detail, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        absentee_notification.setOnClickListener {
            navigationManager.goTo(EditMemberFragment.forMember(member.id))
        }

        replace_card_notification.setOnClickListener {
            navigationManager.goTo(EditMemberFragment.forMember(member.id))
        }

        RecyclerViewHelper.setRecyclerView(household_members_list, memberAdapter, context, false)
    }

    private fun launchClinicNumberDialog() {
        val builder = AlertDialog.Builder(context)
        val editTextLayout = LayoutInflater.from(context).inflate(R.layout.dialog_clinic_number, null)
        builder.setView(editTextLayout)
                .setMessage(R.string.clinic_number_prompt)
                .setPositiveButton(R.string.clinic_number_button) { dialog, _ ->
                    createIdentificationEvent(dialog as AlertDialog).subscribe({
                        navigationManager.popTo(CurrentPatientsFragment.withSnackbarMessage(
                                getString(R.string.checked_in_snackbar_message, member.name)
                        ))
                    }, {
                        logger.error(it)
                    })
                }

        val dialog = builder.create()

        dialog.setOnShowListener {
            val clinicNumberField = dialog.findViewById<TextInputEditText>(R.id.clinic_number_field_text_edit)
            clinicNumberField?.let { keyboardManager.showKeyboard(it) }
            val submitButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            submitButton.isEnabled = false
            clinicNumberField?.addTextChangedListener( LayoutHelper.OnChangedListener { text ->
                submitButton.isEnabled = !text.isEmpty()
            })
        }

        dialog.show()
    }

    private fun createIdentificationEvent(dialog: AlertDialog): Completable {
        val radioGroupView = dialog.findViewById<RadioGroup>(R.id.radio_group_clinic_number)
        val selectedRadioButton = dialog.findViewById<RadioButton>(radioGroupView?.checkedRadioButtonId!!)
        val clinicNumberField = dialog.findViewById<TextInputEditText>(R.id.clinic_number_field_text_edit)

        val clinicNumberType = IdentificationEvent.ClinicNumberType.valueOf(
                selectedRadioButton?.text.toString().toUpperCase())
        val clinicNumber = Integer.valueOf(clinicNumberField?.text.toString())

        val idEvent = IdentificationEvent(id = UUID.randomUUID(),
                                          memberId = member.id,
                                          occurredAt = clock.instant(),
                                          searchMethod = searchFields.searchMethod,
                                          throughMemberId = searchFields.throughMemberId,
                                          clinicNumber = clinicNumber,
                                          clinicNumberType = clinicNumberType)
        return createIdentificationEventUseCase.execute(idEvent)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        menu?.let {
            it.findItem(R.id.menu_member_edit).isVisible = true
            it.findItem(R.id.menu_enroll_newborn).isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                navigationManager.goTo(CurrentPatientsFragment())
            }
            R.id.menu_member_edit -> {
                navigationManager.goTo(EditMemberFragment.forMember(member.id))
            }
            R.id.menu_enroll_newborn -> {
                val member = arguments?.getSerializable(PARAM_MEMBER) as Member
                navigationManager.goTo(EnrollNewbornFragment.forParent(member))
            }
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }
}

