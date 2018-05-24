package org.watsi.uhp.fragments

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_capture_photo.photo
import kotlinx.android.synthetic.main.fragment_capture_photo.photo_btn
import kotlinx.android.synthetic.main.fragment_capture_photo.save_button
import org.threeten.bp.Clock
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Member
import org.watsi.domain.repositories.MemberRepository
import org.watsi.domain.repositories.PhotoRepository
import org.watsi.domain.usecases.CreateMemberUseCase
import org.watsi.uhp.R
import org.watsi.uhp.activities.SavePhotoActivity
import org.watsi.uhp.managers.NavigationManager
import java.util.UUID
import javax.inject.Inject

class EnrollNewbornPhotoFragment : DaggerFragment() {

    @Inject lateinit var clock: Clock
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var photoRepository: PhotoRepository
    @Inject lateinit var memberRepository: MemberRepository
    @Inject lateinit var createMemberUseCase: CreateMemberUseCase
    @Inject lateinit var logger: Logger

    private var photoIds: Pair<UUID, UUID>? = null

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

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.enroll_newborn_photo_label)
        return inflater?.inflate(R.layout.fragment_capture_photo, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        photo_btn.setOnClickListener {
            startActivityForResult(Intent(activity, SavePhotoActivity::class.java), CAPTURE_PHOTO_INTENT)
        }

        save_button.setOnClickListener {
            val newborn = arguments.getSerializable(PARAM_MEMBER) as Member
            createMemberUseCase.execute(newborn.copy(photoId = photoIds?.first))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                navigationManager.popTo(CurrentPatientsFragment())
                Toast.makeText(activity, "Enrollment completed", Toast.LENGTH_LONG).show()
            }, {
                // TODO: handle error
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val (photoIds, error) = SavePhotoActivity.parseResult(resultCode, data, logger)
        photoIds?.let {
            this.photoIds = it
            // TODO: use PhotoLoader
            photoRepository.find(it.second).observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ thumbnailPhoto ->
                        val thumbnailBitmap = BitmapFactory.decodeByteArray(
                                thumbnailPhoto.bytes, 0, thumbnailPhoto.bytes.size)
                        photo.setImageBitmap(thumbnailBitmap)
                    }, {
                        // TODO: handle error
                    })
        }
        error?.let {
            // TODO: handle error
        }
    }
}
