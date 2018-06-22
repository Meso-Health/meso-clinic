package org.watsi.uhp.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import io.reactivex.CompletableObserver
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_edit_member.card_id_field
import kotlinx.android.synthetic.main.fragment_edit_member.edit_birthdate_dialog_field
import kotlinx.android.synthetic.main.fragment_edit_member.fingerprints_field
import kotlinx.android.synthetic.main.fragment_edit_member.gender_field
import kotlinx.android.synthetic.main.fragment_edit_member.member_panel_header
import kotlinx.android.synthetic.main.fragment_edit_member.missing_fingerprints_field
import kotlinx.android.synthetic.main.fragment_edit_member.missing_information_panel_header
import kotlinx.android.synthetic.main.fragment_edit_member.missing_photo_container
import kotlinx.android.synthetic.main.fragment_edit_member.name_field
import kotlinx.android.synthetic.main.fragment_edit_member.phone_number_field
import kotlinx.android.synthetic.main.fragment_edit_member.photo_container
import kotlinx.android.synthetic.main.fragment_edit_member.preferred_language_field
import kotlinx.android.synthetic.main.fragment_edit_member.top_gender_age
import kotlinx.android.synthetic.main.fragment_edit_member.top_name
import kotlinx.android.synthetic.main.fragment_edit_member.top_photo
import org.threeten.bp.Clock
import org.watsi.device.managers.FingerprintManager
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Member
import org.watsi.uhp.R
import org.watsi.uhp.activities.SavePhotoActivity
import org.watsi.uhp.activities.ScanNewCardActivity
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
    private var memberPhotoCornerRadius = 0

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
        memberPhotoCornerRadius = resources.getDimensionPixelSize(R.dimen.cornerRadius)

        genderOptions = listOf(Pair(Member.Gender.F, getString(R.string.female)),
                Pair(Member.Gender.M, getString(R.string.male)))

        val memberId = UUID.fromString(arguments.getString(PARAM_MEMBER_ID))
        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(EditMemberViewModel::class.java)

        val observable = viewModel.getObservable(memberId)
        observable.observe(this, Observer { viewState ->
            viewState?.memberWithThumbnail?.let { memberWithThumbnail ->
                val member = memberWithThumbnail.member
                activity.title = member.name
                top_name.text = member.name

                val photo = memberWithThumbnail.photo
                if (photo != null) {
                    val thumbnailBitmap = BitmapFactory.decodeByteArray(
                            photo.bytes, 0, photo.bytes.size)
                    listOf(photo_container, missing_photo_container).forEach {
                        it.setPhotoPreview(thumbnailBitmap)
                    }
                    top_photo.setPadding(0, 0, 0, 0)
                    val bitmap = BitmapFactory.decodeByteArray(photo.bytes, 0, photo.bytes.size)
                    val roundedDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap)
                    roundedDrawable.cornerRadius = memberPhotoCornerRadius.toFloat()
                    top_photo.setImageDrawable(roundedDrawable)
                } else {
                    top_photo.setPadding(placeholderPhotoIconPadding, placeholderPhotoIconPadding,
                            placeholderPhotoIconPadding, placeholderPhotoIconPadding)
                    if (member.gender == Member.Gender.F) {
                        top_photo.setImageResource(R.drawable.ic_member_placeholder_female)
                    } else {
                        top_photo.setImageResource(R.drawable.ic_member_placeholder_male)
                    }
                }

                val genderString = genderOptions.find { it.first == member.gender }?.second
                top_gender_age.text = resources.getString(R.string.member_list_item_gender_age,
                        genderString,
                        member.getDisplayAge(clock))

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
                    if (photo == null) {
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
}
