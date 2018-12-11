package org.watsi.uhp.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.ethiopia.fragment_edit_member.birthdate_field
import kotlinx.android.synthetic.ethiopia.fragment_edit_member.check_in_button
import kotlinx.android.synthetic.ethiopia.fragment_edit_member.medical_record_number_field
import kotlinx.android.synthetic.ethiopia.fragment_edit_member.membership_number_field
import kotlinx.android.synthetic.ethiopia.fragment_edit_member.name_field
import kotlinx.android.synthetic.ethiopia.fragment_edit_member.photo_container
import kotlinx.android.synthetic.ethiopia.fragment_edit_member.top_gender_age
import kotlinx.android.synthetic.ethiopia.fragment_edit_member.top_name
import kotlinx.android.synthetic.ethiopia.fragment_edit_member.top_photo
import org.threeten.bp.Clock
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.entities.Member
import org.watsi.domain.usecases.CreateIdentificationEventUseCase
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.activities.SavePhotoActivity
import org.watsi.uhp.helpers.MemberStringHelper
import org.watsi.uhp.helpers.PhotoLoader
import org.watsi.uhp.managers.KeyboardManager
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.EditMemberViewModel
import java.util.UUID
import javax.inject.Inject

class EditMemberFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var keyboardManager: KeyboardManager
    @Inject lateinit var clock: Clock
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var logger: Logger
    @Inject lateinit var createIdentificationEventUseCase: CreateIdentificationEventUseCase

    private lateinit var viewModel: EditMemberViewModel
    private lateinit var searchMethod: IdentificationEvent.SearchMethod

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
        val paramMember = arguments.getSerializable(PARAM_MEMBER) as Member
        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(EditMemberViewModel::class.java)

        viewModel.getObservable(paramMember).observe(this, Observer { viewState ->
            viewState?.memberWithThumbnail?.let { memberWithThumbnail ->
                val member = memberWithThumbnail.member
                activity.title = member.name
                top_name.text = member.name

                val photo = memberWithThumbnail.photo
                PhotoLoader.loadMemberPhoto(photo?.bytes, top_photo, activity, member.gender)

                top_gender_age.text = MemberStringHelper.formatAgeAndGender(member, activity, clock)

                membership_number_field.setText(member.membershipNumber)
                name_field.setText(member.name)
                birthdate_field.setText(MemberStringHelper.getDisplayAge(member, activity, clock))
                medical_record_number_field.setValue(member.medicalRecordNumber)
            }

            viewState?.isCheckedIn?.let { isCheckedIn ->
                if (isCheckedIn) {
                    check_in_button.isEnabled = false
                    check_in_button.text = getString(R.string.checked_in)
                } else {
                    check_in_button.isEnabled = true
                    check_in_button.text = getString(R.string.check_in)
                }
                // enable visibility after setting correct button text and color to prevent split-second change
                check_in_button.visibility = View.VISIBLE
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(getString(R.string.blank), R.drawable.ic_arrow_back_white_24dp)
        return inflater?.inflate(R.layout.fragment_edit_member, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        medical_record_number_field.configureEditTextDialog(keyboardManager, { medicalRecordNumberString, dialog ->
            viewModel.updateMedicalRecordNumber(medicalRecordNumberString).subscribe(UpdateFieldObserver(dialog))
        })

        photo_container.setOnClickListener {
            startActivityForResult(Intent(activity, SavePhotoActivity::class.java), CAPTURE_PHOTO_INTENT)
        }

        check_in_button.setOnClickListener {
            createIdentificationEvent().subscribe({
                getMember()?.let {
                    navigationManager.popTo(HomeFragment.withSnackbarMessage(
                        getString(R.string.checked_in_snackbar_message, it.name)
                    ))
                }
            }, {
                logger.error(it)
            })
        }
    }

    private fun createIdentificationEvent(): Completable {
        return getMember()?.let {
            val idEvent = IdentificationEvent(
                id = UUID.randomUUID(),
                memberId = it.id,
                occurredAt = clock.instant(),
                searchMethod = searchMethod,
                throughMemberId = null,
                clinicNumber = null,
                clinicNumberType = null,
                fingerprintsVerificationTier = null,
                fingerprintsVerificationConfidence = null,
                fingerprintsVerificationResultCode = null
            )
            return createIdentificationEventUseCase.execute(idEvent)
        } ?: Completable.complete()
    }

    private fun getMember(): Member? {
        return viewModel.liveData.value?.memberWithThumbnail?.member
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
