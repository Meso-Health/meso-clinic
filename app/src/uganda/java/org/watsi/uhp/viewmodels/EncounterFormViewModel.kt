package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.EncounterForm
import org.watsi.domain.entities.Photo
import org.watsi.domain.relations.EncounterBuilder
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

    fun initEncounterFormPhotos(initialEncounterForms: List<EncounterForm>): Completable {
        return Completable.fromAction {
            val encounterPhotos = mutableListOf<EncounterFormPhoto>()
            initialEncounterForms.map { encounterForm ->
                encounterForm.photoId?.let {  photoId ->
                    encounterForm.thumbnailId?.let { thumbnailId ->
                        val photo = loadPhotoUseCase.execute(photoId).blockingGet()
                        encounterPhotos.add(EncounterFormPhoto(photoId, thumbnailId, photo))
                    }
                }
            }
            observable.postValue(observable.value?.copy(encounterFormPhotos = encounterPhotos))
        }.subscribeOn(Schedulers.io())
    }

    fun addEncounterFormPhoto(fullsizePhotoId: UUID, thumbnailPhotoId: UUID) {
        loadPhotoUseCase.execute(thumbnailPhotoId).subscribe({ thumbnailPhoto ->
            currentEncounterFormPhotos()?.let {
                val updatedPhotos = it.plus(EncounterFormPhoto(fullsizePhotoId, thumbnailPhotoId, thumbnailPhoto))
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

    fun updateEncounterWithForms(encounterBuilder: EncounterBuilder) {
        val encounterFormPhotos = observable.value?.encounterFormPhotos.orEmpty()
        encounterBuilder.encounterForms = encounterFormPhotos.map {
            EncounterForm(UUID.randomUUID(), encounterBuilder.encounter.id, it.fullsizePhotoId, it.thumbnailPhotoId)
        }
    }


    fun currentEncounterFormPhotos(): List<EncounterFormPhoto>? {
        return observable.value?.encounterFormPhotos
    }

    data class ViewState(val encounterFormPhotos: List<EncounterFormPhoto> = emptyList())

    data class EncounterFormPhoto(val fullsizePhotoId: UUID, val thumbnailPhotoId: UUID, val thumbnailPhoto: Photo)
}
