package org.watsi.uhp.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_capture_photo.photo_btn
import kotlinx.android.synthetic.main.fragment_capture_photo.save_button
import org.threeten.bp.Clock

import org.watsi.domain.entities.Member
import org.watsi.domain.entities.Photo
import org.watsi.domain.repositories.IdentificationEventRepository
import org.watsi.domain.repositories.MemberRepository
import org.watsi.domain.repositories.PhotoRepository
import org.watsi.uhp.R
import org.watsi.uhp.helpers.FileProviderHelper
import org.watsi.uhp.listeners.CapturePhotoClickListener
import org.watsi.uhp.managers.ExceptionManager
import org.watsi.uhp.managers.NavigationManager

import java.io.IOException
import java.util.UUID

import javax.inject.Inject

class EnrollmentMemberPhotoFragment : DaggerFragment() {

    @Inject lateinit var clock: Clock
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var memberRepository: MemberRepository
    @Inject lateinit var photoRepository: PhotoRepository
    @Inject lateinit var identificationEventRepository: IdentificationEventRepository

    lateinit var member: Member
    lateinit var photoUri: Uri
    private var photo: Photo? = null

    companion object {
        const val CAPTURE_PHOTO_INTENT = 1
        const val PARAM_MEMBER = "member"

        fun forMember(member: Member): EnrollmentMemberPhotoFragment {
            val fragment = EnrollmentMemberPhotoFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_MEMBER, member)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        member = arguments.getSerializable(PARAM_MEMBER) as Member
        val filename = clock.instant().toString() + ".jpg" // TODO: fix filename
        photoUri = FileProviderHelper.getUriFromProvider(filename, activity)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.enrollment_member_photo_fragment_label)
        return inflater?.inflate(R.layout.fragment_capture_photo, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        if (!member.requiresFingerprint(clock)) {
            save_button.text = getString(R.string.enrollment_complete_btn)
        }

        photo_btn.text = getString(R.string.enrollment_member_photo_btn)
        photo_btn.setOnClickListener(
                CapturePhotoClickListener(CAPTURE_PHOTO_INTENT, this, photoUri))

        save_button.setOnClickListener {
            photo?.let {
                photoRepository.create(it)
            }

            val updatedMember = member.copy(photoId = photo?.id)
            if (!member.requiresFingerprint(clock)) {
                val builder = AlertDialog.Builder(context)
                builder.setMessage(R.string.enrollment_fingerprint_confirm_completion)
                builder.setPositiveButton(android.R.string.yes) { _, _ ->
                    memberRepository.save(updatedMember)

                    identificationEventRepository.openCheckIn(member.id).subscribe({
                        navigationManager.goTo(CurrentMemberDetailFragment.forIdentificationEvent(it))
                    }, {
                        // TODO: handle error
                    }, {
                        navigationManager.goTo(CheckInMemberDetailFragment.forMember(updatedMember))
                    })

                    Toast.makeText(activity, "Enrollment completed", Toast.LENGTH_LONG).show()
                }
                builder.setNegativeButton(android.R.string.no) { dialog, _ -> dialog.dismiss() }
                builder.show()
            } else {
                navigationManager.goTo(EnrollmentContactInfoFragment.forMember(updatedMember))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            if (requestCode == CAPTURE_PHOTO_INTENT && resultCode == Activity.RESULT_OK) {
                // TODO: delete duplicate if necessary

                val bitmap = MediaStore.Images.Media.getBitmap(activity.contentResolver, photoUri)
                val view = view
                if (view != null) (view.findViewById<View>(R.id.photo) as ImageView).setImageBitmap(bitmap)

                photo = Photo(id = UUID.randomUUID(), bytes = null, url = photoUri.toString())
            } else {
                ExceptionManager.reportErrorMessage("Image capture intent failed")
                Toast.makeText(context, R.string.image_capture_failed, Toast.LENGTH_LONG).show()
            }

        } catch (e: IOException) {
            ExceptionManager.reportException(e)
            Toast.makeText(context, R.string.image_failed_to_save, Toast.LENGTH_LONG).show()
        }
    }
}
