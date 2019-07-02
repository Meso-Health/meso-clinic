package org.watsi.uhp.fragments

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import io.reactivex.CompletableObserver
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.ethiopia.fragment_edit_member.birthdate_field
import kotlinx.android.synthetic.ethiopia.fragment_edit_member.check_in_button
import kotlinx.android.synthetic.ethiopia.fragment_edit_member.follow_up_date_container
import kotlinx.android.synthetic.ethiopia.fragment_edit_member.hospital_check_in_details_container
import kotlinx.android.synthetic.ethiopia.fragment_edit_member.inbound_referral_date_container
import kotlinx.android.synthetic.ethiopia.fragment_edit_member.medical_record_number_field
import kotlinx.android.synthetic.ethiopia.fragment_edit_member.membership_number_field
import kotlinx.android.synthetic.ethiopia.fragment_edit_member.name_field
import kotlinx.android.synthetic.ethiopia.fragment_edit_member.needs_renewal_notification
import kotlinx.android.synthetic.ethiopia.fragment_edit_member.photo_container
import kotlinx.android.synthetic.ethiopia.fragment_edit_member.start_claim_button
import kotlinx.android.synthetic.ethiopia.fragment_edit_member.top_gender_age
import kotlinx.android.synthetic.ethiopia.fragment_edit_member.top_name
import kotlinx.android.synthetic.ethiopia.fragment_edit_member.top_photo
import kotlinx.android.synthetic.ethiopia.fragment_edit_member.visit_reason_spinner
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.watsi.device.managers.Logger
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.entities.Member
import org.watsi.domain.repositories.IdentificationEventRepository
import org.watsi.domain.usecases.ValidateDiagnosesAndBillablesExistenceUseCase
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.activities.SavePhotoActivity
import org.watsi.uhp.flowstates.EncounterFlowState
import org.watsi.uhp.helpers.EnumHelper
import org.watsi.uhp.helpers.PhotoLoader
import org.watsi.uhp.helpers.StringHelper
import org.watsi.uhp.managers.KeyboardManager
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.EditMemberViewModel
import java.util.UUID
import javax.inject.Inject

class EditMemberFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var keyboardManager: KeyboardManager
    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var clock: Clock
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var logger: Logger
    @Inject lateinit var identificationEventRepository: IdentificationEventRepository
    @Inject lateinit var validateDiagnosesAndBillablesExistenceUseCase: ValidateDiagnosesAndBillablesExistenceUseCase

    private lateinit var viewModel: EditMemberViewModel
    private lateinit var paramMember: Member
    private lateinit var searchMethod: IdentificationEvent.SearchMethod
    private lateinit var observable: LiveData<EditMemberViewModel.ViewState>

    private var placeholderPhotoIconPadding = 0
    private var memberPhotoCornerRadius = 0

    companion object {
        const val CAPTURE_PHOTO_INTENT = 1
        const val PARAM_MEMBER = "member"
        const val PARAM_SEARCH_METHOD = "search_method"

        fun forParams(
            member: Member,
            searchMethod: IdentificationEvent.SearchMethod
        ): EditMemberFragment {
            val editMemberFragment = EditMemberFragment()
            editMemberFragment.arguments = Bundle().apply {
                putSerializable(PARAM_MEMBER, member)
                putSerializable(PARAM_SEARCH_METHOD, searchMethod)
            }
            return editMemberFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        placeholderPhotoIconPadding = resources.getDimensionPixelSize(R.dimen.editMemberPhotoPlaceholderPadding)
        memberPhotoCornerRadius = resources.getDimensionPixelSize(R.dimen.cornerRadius)

        searchMethod = arguments.getSerializable(PARAM_SEARCH_METHOD) as IdentificationEvent.SearchMethod
        paramMember = arguments.getSerializable(PARAM_MEMBER) as Member
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(EditMemberViewModel::class.java)
        observable = viewModel.getObservable(paramMember)
        observable.observe(this, Observer {
            it?.let { viewState ->
                setErrors(viewState.validationErrors)

                viewState.memberWithThumbnail?.let { memberWithThumbnail ->
                    val member = memberWithThumbnail.member
                    val photo = memberWithThumbnail.photo

                    if (member.needsRenewal == true) {
                        needs_renewal_notification.visibility = View.VISIBLE
                    }

                    PhotoLoader.loadMemberPhoto(
                        bytes = photo?.bytes,
                        view = top_photo,
                        context = activity,
                        gender = member.gender,
                        photoExists = member.photoExists(),
                        placeholderPadding = placeholderPhotoIconPadding
                    )

                    top_name.text = member.name
                    top_gender_age.text = StringHelper.formatAgeAndGender(member, activity, clock)

                    membership_number_field.setText(member.membershipNumber)
                    name_field.setText(member.name)
                    birthdate_field.setText(StringHelper.getDisplayAge(member, activity, clock))
                    medical_record_number_field.setValue(member.medicalRecordNumber)

                    photo?.let {
                        val thumbnailBitmap = BitmapFactory.decodeByteArray(
                            photo.bytes, 0, photo.bytes.size)
                        photo_container.setPhotoPreview(thumbnailBitmap)
                    }

                    activity.invalidateOptionsMenu()
                }

                viewState.isCheckedIn?.let { isCheckedIn ->
                    if (isCheckedIn && sessionManager.userHasPermission(SessionManager.Permissions.WORKFLOW_CLAIMS_PREPARATION)) {
                        start_claim_button.visibility = View.VISIBLE
                    } else if (!isCheckedIn && (sessionManager.userHasPermission(SessionManager.Permissions.WORKFLOW_CLINIC_IDENTIFICATION)
                                    || sessionManager.userHasPermission(SessionManager.Permissions.WORKFLOW_HOSPITAL_IDENTIFICATION))) {
                        if (sessionManager.currentUser()?.isHospital() == true) {
                            hospital_check_in_details_container.visibility = View.VISIBLE
                        }
                        check_in_button.visibility = View.VISIBLE
                    }
                }

                viewState.visitReason?.let { visitReason ->
                    when (visitReason) {
                        Encounter.VisitReason.REFERRAL -> {
                            inbound_referral_date_container.visibility = View.VISIBLE
                            follow_up_date_container.visibility = View.GONE
                            // manually set to today when datepicker first appears since onChange won't be called
                            if (viewState.inboundReferralDate == null) {
                                viewModel.onInboundReferralDateChange(LocalDate.now(clock))
                            }
                        }
                        Encounter.VisitReason.FOLLOW_UP -> {
                            inbound_referral_date_container.visibility = View.GONE
                            follow_up_date_container.visibility = View.VISIBLE
                            // manually set to today when datepicker first appears since onChange won't be called
                            if (viewState.followUpDate == null) {
                                viewModel.onFollowUpDateChange(LocalDate.now(clock))
                            }
                        }
                        else -> {
                            inbound_referral_date_container.visibility = View.GONE
                            follow_up_date_container.visibility = View.GONE
                        }
                    }
                }
            }
        })
    }

    private fun setErrors(errors: Map<String, Int>) {
        errors[EditMemberViewModel.MEDICAL_RECORD_NUMBER_ERROR].let { errorResourceId ->
            medical_record_number_field.setErrorOnField(errorResourceId?.let { getString(errorResourceId) })
        }

        errors[EditMemberViewModel.VISIT_REASON_ERROR].let { errorResourceId ->
            visit_reason_spinner.setError(errorResourceId?.let { getString(errorResourceId) })
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(paramMember.name, R.drawable.ic_arrow_back_white_24dp)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_edit_member, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        medical_record_number_field.configureEditTextDialog(
            keyboardManager = keyboardManager,
            handleNewValue = { medicalRecordNumberString, dialog ->
                viewModel.updateMedicalRecordNumber(medicalRecordNumberString)
                    .subscribe(UpdateFieldObserver(dialog))
            },
            validateFieldAndReturnError = { medicalRecordNumberString ->
                viewModel.validateMedicalRecordNumber(medicalRecordNumberString,
                    getString(R.string.medical_record_number_length_validation_error))
            },
            maxTextLength = Member.MAX_MRN_LENGTH
        )

        photo_container.setOnClickListener {
            startActivityForResult(Intent(activity, SavePhotoActivity::class.java), CAPTURE_PHOTO_INTENT)
        }

        if (sessionManager.currentUser()?.isHospital() == true) {
            val visitReasonMappings = EnumHelper.getVisitReasonMappings(sessionManager.currentUser()?.providerType, logger)
            val visitReasonEnums = visitReasonMappings.map { it.first }
            val visitReasonStrings = visitReasonMappings.map { getString(it.second) }

            visit_reason_spinner.setUpWithPrompt(
                choices = visitReasonStrings,
                initialChoice = null,
                onItemSelected = { index: Int -> viewModel.onVisitReasonChange(visitReasonEnums[index]) },
                promptString = getString(R.string.visit_reason_prompt),
                onPromptSelected = { viewModel.onVisitReasonChange(null) }
            )

            inbound_referral_date_container.setUp(
                initialValue = Instant.now(),
                clock = clock,
                onDateSelected = { date ->
                    viewModel.onInboundReferralDateChange(
                        LocalDateTime.ofInstant(date, clock.zone).toLocalDate()
                    )
                }
            )

            follow_up_date_container.setUp(
                initialValue = Instant.now(),
                clock = clock,
                onDateSelected = { date ->
                    viewModel.onFollowUpDateChange(
                        LocalDateTime.ofInstant(date, clock.zone).toLocalDate()
                    )
                }
            )
        }

        check_in_button.setOnClickListener {
            viewModel.getMember()?.let { member ->
                sessionManager.currentUser()?.let { user ->
                    viewModel.validateAndCheckInMember(searchMethod, user).subscribe({
                        navigationManager.popTo(HomeFragment.withSnackbarMessage(
                            getString(R.string.checked_in_snackbar_message, member.name)
                        ))
                    }, { throwable ->
                        if (throwable is EditMemberViewModel.ValidationException) {
                            // do nothing for now. No need to say "some fields are invalid"
                        } else {
                            logger.error(throwable)
                        }
                    })
                }
            }
        }

        // This button is gated on needing claims preparation permission. It is never set to visible
        // if the permission is not available
        start_claim_button.setOnClickListener {
            viewModel.getMember()?.let { member ->
                Single.fromCallable {
                    validateDiagnosesAndBillablesExistenceUseCase.execute().blockingAwait()
                    identificationEventRepository.openCheckIn(member.id).blockingGet()
                }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe( { idEvent ->
                    val encounter = Encounter(
                        id = UUID.randomUUID(),
                        memberId = member.id,
                        identificationEventId = idEvent.id,
                        occurredAt = Instant.now(clock),
                        patientOutcome = null
                    )
                    navigationManager.goTo(VisitTypeFragment.forEncounter(
                        EncounterFlowState(
                            encounter = encounter,
                            encounterItemRelations = emptyList(),
                            encounterForms = emptyList(),
                            referral = null,
                            diagnoses = emptyList(),
                            member = member
                        )
                    ))
                }, {
                    if (it.cause is ValidateDiagnosesAndBillablesExistenceUseCase.BillableAndDiagnosesMissingException) {
                        showDiagnosisAndBillableMissingDialog()
                    } else {
                        logger.error(it)
                    }
                })
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        observable.value?.let { viewState ->
            if (viewState.isCheckedIn != null && viewState.isCheckedIn) {
                menu?.let { it.findItem(R.id.menu_check_out_member).isVisible = true }
            } else {
                menu?.let { it.findItem(R.id.menu_check_out_member).isVisible = false }
            }
        }
    }

    fun showDiagnosisAndBillableMissingDialog() {
        android.app.AlertDialog.Builder(activity)
                .setMessage(getString(R.string.diagnoses_and_billable_missing_dialog))
                .create().show()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_check_out_member -> {
                android.app.AlertDialog.Builder(activity)
                        .setTitle(R.string.check_out_alert_dialog_title)
                        .setMessage(getString(R.string.check_out_alert_dialog_message, paramMember.name))
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.delete) { _, _ ->
                            viewModel.dismissIdentificationEvent().subscribe {
                                navigationManager.popTo(
                                    HomeFragment.withSnackbarMessage(
                                        getString(R.string.checked_out_snackbar_message, paramMember.name)
                                    )
                                )
                            }
                        }.create().show()
            }
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

    /**
     * CompletableObserver for dismissing the open AlertDialog if successful or displaying an
     * error message if an error occurs
     */
    inner class UpdateFieldObserver(
        private val dialog: AlertDialog,
        private val layoutId: Int = R.id.dialog_input_layout
    ) : CompletableObserver {
        override fun onComplete() {
            dialog.dismiss()
        }

        override fun onSubscribe(d: Disposable) { /* no-op */ }

        override fun onError(e: Throwable) {
            val layout = dialog.findViewById<TextInputLayout>(layoutId)
            layout?.error = e.localizedMessage
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CAPTURE_PHOTO_INTENT -> {
                val (photoIds, _) = SavePhotoActivity.parseResult(resultCode, data, logger)
                if (photoIds != null) {
                    viewModel.updatePhoto(photoIds.first, photoIds.second).subscribe()
                }
            }
            else -> {
                logger.error("Unknown requestCode called from EditMemberFragment: $requestCode")
            }
        }
    }
}
