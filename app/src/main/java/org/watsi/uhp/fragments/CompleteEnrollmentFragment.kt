package org.watsi.uhp.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_complete_enrollment.complete_enrollment_button
import kotlinx.android.synthetic.main.fragment_complete_enrollment.missing_fingerprints_field
import kotlinx.android.synthetic.main.fragment_complete_enrollment.missing_photo_container
import org.threeten.bp.Clock
import org.watsi.device.managers.FingerprintManager
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Member
import org.watsi.domain.repositories.IdentificationEventRepository
import org.watsi.uhp.R
import org.watsi.uhp.activities.SavePhotoActivity
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.CompleteEnrollmentViewModel
import java.util.UUID
import javax.inject.Inject

class CompleteEnrollmentFragment : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var clock: Clock
    @Inject lateinit var logger: Logger
    @Inject lateinit var fingerprintManager: FingerprintManager
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var identificationEventRepository: IdentificationEventRepository
    private lateinit var viewModel: CompleteEnrollmentViewModel
    private lateinit var memberId: UUID
    private lateinit var member: Member

    companion object {
        const val PARAM_MEMBER_ID = "member_id"
        const val CAPTURE_PHOTO_INTENT = 1
        const val CAPTURE_FINGERPRINT_INTENT = 2

        fun forMember(memberId: UUID): CompleteEnrollmentFragment {
            val editMemberFragment = CompleteEnrollmentFragment()
            editMemberFragment.arguments = Bundle().apply {
                putString(PARAM_MEMBER_ID, memberId.toString())
            }
            return editMemberFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        memberId = UUID.fromString(arguments.getString(PARAM_MEMBER_ID))
        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(CompleteEnrollmentViewModel::class.java)
        viewModel.getObservable(memberId).observe(this, Observer { memberWithThumbnailObservable ->
            memberWithThumbnailObservable?.let { memberWithThumbnail ->
                val photo = memberWithThumbnail.photo

                missing_photo_container.visibility = View.VISIBLE
                if (photo != null) {
                    val thumbnailBitmap = BitmapFactory.decodeByteArray(photo.bytes, 0, photo.bytes.size)
                    missing_photo_container.setPhotoPreview(thumbnailBitmap)
                }

                if (memberWithThumbnail.member.requiresFingerprint(clock)) {
                    missing_fingerprints_field.visibility = View.VISIBLE
                }
                missing_fingerprints_field.toggleEnabled(true)
                missing_fingerprints_field.setFingerprints(memberWithThumbnail.member.fingerprintsGuid)

                member = memberWithThumbnail.member
             }
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.complete_enrollment)
        return inflater?.inflate(R.layout.fragment_complete_enrollment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        missing_fingerprints_field.setOnClickListener {
            if (!fingerprintManager.captureFingerprint(memberId.toString(), this, CAPTURE_FINGERPRINT_INTENT)) {
                missing_fingerprints_field.setError(context.getString(R.string.fingerprints_not_installed_error_message))
            }
        }

        missing_photo_container.setOnClickListener {
            startActivityForResult(Intent(activity, SavePhotoActivity::class.java), CAPTURE_PHOTO_INTENT)
        }

        complete_enrollment_button.setOnClickListener {
            identificationEventRepository.openCheckIn(memberId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({idEvent ->
                    navigationManager.popTo(CurrentMemberDetailFragment.forMemberAndIdEvent(member, idEvent))
                    Toast.makeText(activity, "Enrollment completed", Toast.LENGTH_LONG).show()
                }, {
                    // TODO: handle error
                    logger.error(it)
                }, {
                    navigationManager.popTo(CheckInMemberDetailFragment.forMember(member))
                    Toast.makeText(activity, "Enrollment completed", Toast.LENGTH_LONG).show()
                })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CAPTURE_FINGERPRINT_INTENT -> {
                val fingerprintResponse = fingerprintManager.parseResponse(resultCode, data)
                when (fingerprintResponse.status) {
                    FingerprintManager.FingerprintStatus.SUCCESS -> {
                        val fingerprintId = fingerprintResponse.fingerprintId
                        if (fingerprintId != null) {
                            viewModel.updateFingerprints(fingerprintId).subscribe()
                        } else {
                            logger.error("FingerprintManager returned null fingerprintId $fingerprintResponse")
                        }
                    }
                    FingerprintManager.FingerprintStatus.FAILURE -> {
                        missing_fingerprints_field.setError(context.getString(R.string.fingerprints_error_message))
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
            else -> {
                logger.error("Unknown requestCode called from EditMemberFragment: $requestCode")
            }
        }
    }
}