package org.watsi.uhp.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import io.reactivex.CompletableObserver
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.uganda.fragment_edit_member.card_id_field
import kotlinx.android.synthetic.uganda.fragment_edit_member.edit_birthdate_dialog_field
import kotlinx.android.synthetic.uganda.fragment_edit_member.fingerprints_field
import kotlinx.android.synthetic.uganda.fragment_edit_member.gender_field
import kotlinx.android.synthetic.uganda.fragment_edit_member.member_panel_header
import kotlinx.android.synthetic.uganda.fragment_edit_member.missing_fingerprints_field
import kotlinx.android.synthetic.uganda.fragment_edit_member.missing_information_panel_header
import kotlinx.android.synthetic.uganda.fragment_edit_member.missing_photo_container
import kotlinx.android.synthetic.uganda.fragment_edit_member.name_field
import kotlinx.android.synthetic.uganda.fragment_edit_member.phone_number_field
import kotlinx.android.synthetic.uganda.fragment_edit_member.photo_container
import kotlinx.android.synthetic.uganda.fragment_edit_member.preferred_language_field
import kotlinx.android.synthetic.uganda.fragment_edit_member.top_gender_age
import kotlinx.android.synthetic.uganda.fragment_edit_member.top_name
import kotlinx.android.synthetic.uganda.fragment_edit_member.top_photo
import org.threeten.bp.Clock
import org.watsi.device.managers.FingerprintManager
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Member
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.activities.SavePhotoActivity
import org.watsi.uhp.activities.ScanNewCardActivity
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
    @Inject lateinit var fingerprintManager: FingerprintManager
    @Inject lateinit var clock: Clock
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var logger: Logger

    private lateinit var viewModel: EditMemberViewModel
    private lateinit var genderOptions: List<Pair<Member.Gender, String>>
    private var member: Member? = null
    private var placeholderPhotoIconPadding = 0

    companion object {
        const val PARAM_MEMBER_ID = "member_id"
        const val CAPTURE_PHOTO_INTENT = 1
        const val CAPTURE_FINGERPRINT_INTENT = 2
        const val SCAN_QRCODE_INTENT = 3

        fun forMember(memberId: UUID): EditMemberFragment {
            val editMemberFragment = EditMemberFragment()
            editMemberFragment.arguments = Bundle().apply {
                putString(PARAM_MEMBER_ID, memberId.toString())
            }
            return editMemberFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        placeholderPhotoIconPadding =resources.getDimensionPixelSize(R.dimen.editMemberPhotoPlaceholderPadding)

        genderOptions = listOf(Pair(Member.Gender.F, getString(R.string.female)),
                Pair(Member.Gender.M, getString(R.string.male)))

        val memberId = UUID.fromString(arguments.getString(PARAM_MEMBER_ID))
        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(EditMemberViewModel::class.java)

        val observable = viewModel.getObservable(memberId)
        observable.observe(this, Observer { viewState ->
            viewState?.memberWithThumbnail?.let { memberWithThumbnail ->
                val member = memberWithThumbnail.member

                (activity as ClinicActivity).setToolbar(member.name, R.drawable.ic_arrow_back_white_24dp)
                activity.title = member.name
                top_name.text = member.name

                PhotoLoader.loadMemberPhoto(
                    memberWithThumbnail.photo?.bytes,
                    top_photo,
                    context,
                    member.gender,
                    placeholderPhotoIconPadding
                )

                val genderString = genderOptions.find { it.first == member.gender }?.second
                top_gender_age.text = resources.getString(
                    R.string.member_list_item_gender_age,
                    genderString,
                    StringHelper.getDisplayAge(member, context, clock)
                )

                name_field.setValue(member.name)
                gender_field.setValue(genderString)
                edit_birthdate_dialog_field.setValue(member.birthdate, member.birthdateAccuracy)
                phone_number_field.setValue(member.phoneNumber)
                preferred_language_field.setValue(member.language)

                listOf(fingerprints_field, missing_fingerprints_field).forEach { field ->
                    member.fingerprintsGuid?.let {
                        field.setFingerprints(it)
                    }
                    field.toggleEnabled(member.requiresFingerprint(clock))
                }

                member.cardId?.let {
                    card_id_field.setCardId(Member.formatCardId(it))
                }

                // determine if missing information section should be shown based on initial
                // member information
                if (this.member == null) {
                    if (member.isAbsentee(clock)) {
                        missing_information_panel_header.visibility = View.VISIBLE
                        member_panel_header.visibility = View.VISIBLE
                    }
                    if (memberWithThumbnail.photo == null) {
                        missing_photo_container.visibility = View.VISIBLE
                    }
                    if (member.requiresFingerprint(clock) && member.fingerprintsGuid == null) {
                        missing_fingerprints_field.visibility = View.VISIBLE
                    }
                    this.member = member
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_edit_member, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        name_field.configureEditTextDialog(keyboardManager, { name, dialog ->
            viewModel.updateName(name).subscribe(UpdateFieldObserver(dialog))
        })

        gender_field.configureOptionsDialog(genderOptions.map { it.second }.toTypedArray(), { idx ->
            viewModel.updateGender(genderOptions[idx].first).subscribe()
        })

        edit_birthdate_dialog_field.configureBirthdateDialog(keyboardManager, { birthdate, birthdateAccuracy, dialog ->
            viewModel.updateBirthdate(birthdate, birthdateAccuracy)
                    .subscribe(UpdateFieldObserver(dialog, R.id.age_input_layout))
        })

        phone_number_field.configureEditTextDialog(keyboardManager, { phoneNumberString, dialog ->
            viewModel.updatePhoneNumber(phoneNumberString).subscribe(UpdateFieldObserver(dialog))
        })

        val languageChoices = Member.LANGUAGE_CHOICES.toTypedArray()
        preferred_language_field.configureOptionsDialog(languageChoices, { idx ->
            val language = languageChoices[idx]
            if (language == Member.LANGUAGE_CHOICE_OTHER) {
                preferred_language_field.launchEditTextDialog(
                        keyboardManager,
                        handleNewValue = { other, dialog ->
                            viewModel.updateLanguage(other).subscribe(UpdateFieldObserver(dialog))
                        },
                        defaultValue = { value ->
                            // defaults the "Other" DialogEditField to blank instead of the previous selected common language
                            if (Member.COMMON_LANGUAGES.contains(value)) "" else value
                        }
                )
            } else {
                viewModel.updateLanguage(language).subscribe()
            }
        })

        listOf(photo_container, missing_photo_container).forEach {
            it.setOnClickListener {
                startActivityForResult(Intent(activity, SavePhotoActivity::class.java), CAPTURE_PHOTO_INTENT)
            }
        }

        listOf(fingerprints_field, missing_fingerprints_field).forEach {
            it.setOnClickListener {
                member?.let {
                    if (!fingerprintManager.captureFingerprint(it.id.toString(), this, CAPTURE_FINGERPRINT_INTENT)) {
                        listOf(fingerprints_field, missing_fingerprints_field).forEach { fingerprintsField ->
                            fingerprintsField.setError(context.getString(R.string.fingerprints_not_installed_error_message))
                        }
                    }
                }
            }
        }

        card_id_field.setOnClickListener {
            startActivityForResult(Intent(activity, ScanNewCardActivity::class.java), SCAN_QRCODE_INTENT)
        }
    }

    /**
     * CompletableObserver for dismissing the open AlertDialog if successful or displaying an
     * error message if an error occurs
     */
    inner class UpdateFieldObserver(private val dialog: AlertDialog,
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
            CAPTURE_FINGERPRINT_INTENT -> {
                val fingerprintResponse = fingerprintManager.parseResponseForRegistration(resultCode, data)
                when (fingerprintResponse.status) {
                    FingerprintManager.FingerprintStatus.SUCCESS -> {
                        fingerprintResponse.fingerprintId?.let {
                            viewModel.updateFingerprints(it).subscribe()
                        } ?: run {
                            logger.error("FingerprintManager returned success but null fingerprintId")
                        }
                    }
                    FingerprintManager.FingerprintStatus.FAILURE -> {
                        listOf(fingerprints_field, missing_fingerprints_field).forEach {
                            it.setError(context.getString(R.string.fingerprints_error_message))
                        }
                    }
                    FingerprintManager.FingerprintStatus.CANCELLED -> { /* no-op */ }
                }
            }
            CAPTURE_PHOTO_INTENT -> {
                val (photoIds, error) = SavePhotoActivity.parseResult(resultCode, data, logger)
                if (photoIds != null) {
                    viewModel.updatePhoto(photoIds.first, photoIds.second).subscribe()
                }
            }
            SCAN_QRCODE_INTENT -> {
                val (cardId, error) = ScanNewCardActivity.parseResult(resultCode, data, logger)
                if (cardId != null) {
                    viewModel.updateMemberCard(cardId).subscribe()
                }
            }
            else -> {
                logger.error("Unknown requestCode called from EditMemberFragment: $requestCode")
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                navigationManager.goBack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
