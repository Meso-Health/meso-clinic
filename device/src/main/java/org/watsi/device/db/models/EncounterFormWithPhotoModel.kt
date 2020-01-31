package org.watsi.device.db.models

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation
import org.watsi.domain.relations.EncounterFormWithPhoto

data class EncounterFormWithPhotoModel(
        @Embedded var encounterFormModel: EncounterFormModel? = null,
        @Relation(parentColumn = "photoId", entityColumn = "id", entity = PhotoModel::class)
        var photoModel: List<PhotoModel>? = null) {

    fun toEncounterFormWithPhoto(): EncounterFormWithPhoto {
        encounterFormModel?.toEncounterForm()?.let { encounterForm ->
            photoModel?.firstOrNull()?.toPhoto()?.let { photo ->
                return EncounterFormWithPhoto(encounterForm, photo)
            }
            throw IllegalStateException("PhotoModel cannot be null")
        }
        throw IllegalStateException("EncounterFormModel cannot be null")
    }
}
