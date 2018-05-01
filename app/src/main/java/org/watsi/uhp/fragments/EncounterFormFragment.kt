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
import kotlinx.android.synthetic.main.fragment_encounter_form.add_another_button
import kotlinx.android.synthetic.main.fragment_encounter_form.finish_button
import kotlinx.android.synthetic.main.fragment_encounter_form.photo_btn
import org.threeten.bp.Clock
import org.watsi.device.managers.Logger

import org.watsi.domain.entities.EncounterForm
import org.watsi.domain.entities.Photo
import org.watsi.domain.relations.EncounterWithItemsAndForms
import org.watsi.domain.repositories.PhotoRepository
import org.watsi.uhp.R
import org.watsi.uhp.helpers.FileProviderHelper
import org.watsi.uhp.listeners.CapturePhotoClickListener
import org.watsi.uhp.managers.NavigationManager

import java.io.IOException
import java.util.UUID
import javax.inject.Inject

class EncounterFormFragment : DaggerFragment() {

    @Inject lateinit var clock: Clock
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var photoRepository: PhotoRepository
    @Inject lateinit var logger: Logger

    lateinit var encounter: EncounterWithItemsAndForms
    lateinit var photoUri: Uri
    private var photo: Photo? = null

    companion object {
        const val CAPTURE_PHOTO_INTENT = 1
        const val PARAM_ENCOUNTER = "encounter"

        fun forEncounter(encounter: EncounterWithItemsAndForms): EncounterFormFragment {
            val fragment = EncounterFormFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_ENCOUNTER, encounter)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        encounter = arguments.getSerializable(PARAM_ENCOUNTER) as EncounterWithItemsAndForms
        val filename = clock.instant().toString() + ".jpg" // TODO: fix filename
        photoUri = FileProviderHelper.getUriFromProvider(filename, activity)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.encounter_form_fragment_label)
        return inflater?.inflate(R.layout.fragment_encounter_form, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        photo_btn.setOnClickListener(
                CapturePhotoClickListener(CAPTURE_PHOTO_INTENT, this, photoUri))

        add_another_button.setOnClickListener {
            navigationManager.goTo(EncounterFormFragment.forEncounter(
                    encounter.copy(encounterForms = updatedEncounterFormList())))
        }

        finish_button.setOnClickListener {
            createEncounterForm()
            navigationManager.goTo(ReceiptFragment.forEncounter(
                    encounter.copy(encounterForms = updatedEncounterFormList())))
        }
    }

    private fun createEncounterForm(): EncounterForm? {
        return photo?.let {
            EncounterForm(UUID.randomUUID(), encounter.encounter.id, it.id)
        }
    }

    private fun updatedEncounterFormList(): List<EncounterForm> {
        val list = encounter.encounterForms.toMutableList()
        photo?.let {
            list.add(EncounterForm(UUID.randomUUID(), encounter.encounter.id, it.id))
        }
        return list.toList()
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
