package org.watsi.uhp.fragments

import android.app.Activity
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
import kotlinx.android.synthetic.main.fragment_capture_photo.save_button
import kotlinx.android.synthetic.main.fragment_encounter_form.photo_btn
import org.threeten.bp.Clock
import org.watsi.device.managers.Logger

import org.watsi.domain.entities.Member
import org.watsi.domain.entities.Photo
import org.watsi.domain.repositories.MemberRepository
import org.watsi.domain.repositories.PhotoRepository
import org.watsi.uhp.R
import org.watsi.uhp.helpers.FileProviderHelper
import org.watsi.uhp.listeners.CapturePhotoClickListener
import org.watsi.uhp.managers.NavigationManager

import java.io.IOException
import java.util.UUID

import javax.inject.Inject

class EnrollNewbornPhotoFragment : DaggerFragment() {

    @Inject lateinit var clock: Clock
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var photoRepository: PhotoRepository
    @Inject lateinit var memberRepository: MemberRepository
    @Inject lateinit var logger: Logger

    lateinit var newborn: Member
    lateinit var photoUri: Uri
    private var photo: Photo? = null

    companion object {
        const val CAPTURE_PHOTO_INTENT = 1
        const val PARAM_MEMBER = "member"

        fun forNewborn(member: Member): EnrollNewbornPhotoFragment {
            val fragment = EnrollNewbornPhotoFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_MEMBER, member)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        newborn = arguments.getSerializable(PARAM_MEMBER) as Member
        val filename = clock.instant().toString() + ".jpg" // TODO: fix filename
        photoUri = FileProviderHelper.getUriFromProvider(filename, activity)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.enroll_newborn_photo_label)
        return inflater?.inflate(R.layout.fragment_capture_photo, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        photo_btn.setOnClickListener(
                CapturePhotoClickListener(CAPTURE_PHOTO_INTENT, this, photoUri))

        save_button.setText(R.string.save_btn_label)

        save_button.setOnClickListener {
            memberRepository.save(newborn.copy(photoId = photo?.id))
            navigationManager.popTo(CurrentPatientsFragment())
            Toast.makeText(activity, "Enrollment completed", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            if (requestCode == EnrollmentMemberPhotoFragment.CAPTURE_PHOTO_INTENT && resultCode == Activity.RESULT_OK) {
                // TODO: delete duplicate if necessary

                val bitmap = MediaStore.Images.Media.getBitmap(activity.contentResolver, photoUri)
                val view = view
                if (view != null) (view.findViewById<View>(R.id.photo) as ImageView).setImageBitmap(bitmap)

                photo = Photo(id = UUID.randomUUID(), bytes = null, url = photoUri.toString())
                photo?.let { photoRepository.create(it) }
            } else {
                logger.error("Image capture intent failed")
                Toast.makeText(context, R.string.image_capture_failed, Toast.LENGTH_LONG).show()
            }

        } catch (e: IOException) {
            logger.error(e)
            Toast.makeText(context, R.string.image_failed_to_save, Toast.LENGTH_LONG).show()
        }
    }
}
