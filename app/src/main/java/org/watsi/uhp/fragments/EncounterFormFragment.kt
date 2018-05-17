package org.watsi.uhp.fragments

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_encounter_form.finish_button
import kotlinx.android.synthetic.main.fragment_encounter_form.photo
import kotlinx.android.synthetic.main.fragment_encounter_form.photo_btn
import org.threeten.bp.Clock
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.EncounterForm
import org.watsi.domain.relations.EncounterWithItemsAndForms
import org.watsi.domain.repositories.PhotoRepository
import org.watsi.uhp.R
import org.watsi.uhp.activities.SavePhotoActivity
import org.watsi.uhp.managers.NavigationManager
import java.util.UUID
import javax.inject.Inject

class EncounterFormFragment : DaggerFragment() {

    @Inject lateinit var clock: Clock
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var photoRepository: PhotoRepository
    @Inject lateinit var logger: Logger

    private var photoIds: Pair<UUID, UUID>? = null

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

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.encounter_form_fragment_label)
        return inflater?.inflate(R.layout.fragment_encounter_form, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        photo_btn.setOnClickListener {
            val intent = Intent(activity, SavePhotoActivity::class.java).apply {
                putExtra(SavePhotoActivity.FOR_FORM_KEY, true)
            }
            startActivityForResult(intent, CAPTURE_PHOTO_INTENT)
        }

        finish_button.setOnClickListener {
            val encounter = arguments.getSerializable(PARAM_ENCOUNTER) as EncounterWithItemsAndForms
            val encounterForms = encounter.encounterForms.toMutableList()
            photoIds?.let {
                val encounterForm = EncounterForm(UUID.randomUUID(), encounter.encounter.id, it.first)
                encounterForms.add(encounterForm)
            }
            navigationManager.goTo(ReceiptFragment.forEncounter(
                    encounter.copy(encounterForms = encounterForms)))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val (photoIds, error) = SavePhotoActivity.parseResult(resultCode, data, logger)
        photoIds?.let {
            this.photoIds = it
            photoRepository.find(it.second).observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ thumbnailPhoto ->
                        thumbnailPhoto.bytes?.let { photoBytes ->
                            val thumbnailBitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.size)
                            photo.setImageBitmap(thumbnailBitmap)
                        }
                    }, {
                        // TODO: handle error
                    })
        }
        error?.let {
            // TODO: handle error
        }
    }
}
