package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.EncounterForm
import org.watsi.domain.entities.Photo
import org.watsi.domain.relations.EncounterWithItemsAndForms
import org.watsi.domain.usecases.LoadPhotoUseCase
import java.util.UUID
import javax.inject.Inject

class EncounterFormViewModel @Inject constructor(
        private val loadPhotoUseCase: LoadPhotoUseCase,
        private val logger: Logger
) : ViewModel() {

    private val observable = MutableLiveData<ViewState>()

    init {
        observable.value = ViewState()
    }

    fun getObservable(): LiveData<ViewState> = observable

    fun addEncounterFormPhoto(fullsizePhotoId: UUID, thumbnailPhotoId: UUID) {
        loadPhotoUseCase.execute(thumbnailPhotoId).subscribe({ thumbnailPhoto ->
            currentEncounterFormPhotos()?.let {
                val updatedPhotos = it.plus(EncounterFormPhoto(fullsizePhotoId, thumbnailPhoto))
                observable.postValue(observable.value?.copy(encounterFormPhotos = updatedPhotos))
            }
        }, {
            logger.error(it)
        })
    }

    fun removeEncounterFormPhoto(photo: EncounterFormPhoto) {
        currentEncounterFormPhotos()?.let {
            val updatedPhotos = it.minus(photo)
            observable.value = observable.value?.copy(encounterFormPhotos = updatedPhotos)
        }
    }

    fun updateEncounterWithForms(encounterRelation: EncounterWithItemsAndForms): EncounterWithItemsAndForms {
        val encounterFormPhotos = observable.value?.encounterFormPhotos.orEmpty()
        val encounterForms = encounterFormPhotos.map {
            EncounterForm(UUID.randomUUID(), encounterRelation.encounter.id, it.fullsizePhotoId)
        }
        return encounterRelation.copy(encounterForms = encounterForms)
    }

    fun currentEncounterFormPhotos(): List<EncounterFormPhoto>? {
        return observable.value?.encounterFormPhotos
    }

    data class ViewState(val encounterFormPhotos: List<EncounterFormPhoto> = emptyList())

    data class EncounterFormPhoto(val fullsizePhotoId: UUID, val thumbnailPhoto: Photo)
}
