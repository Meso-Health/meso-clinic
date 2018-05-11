package org.watsi.device.db.models

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation
import org.watsi.domain.relations.EncounterFormWithEncounterAndPhoto

data class EncounterFormWithEncounterAndPhotoModel(
        @Embedded var encounterFormModel: EncounterFormModel? = null,
        @Relation(parentColumn = "encounterId", entityColumn = "id", entity = EncounterModel::class)
        var encounterModel: List<EncounterModel>? = null,
        @Relation(parentColumn = "photoId", entityColumn = "id", entity = PhotoModel::class)
        var photoModel: List<PhotoModel>? = null) {

    fun toEncounterFormWithEncounterAndPhoto(): EncounterFormWithEncounterAndPhoto {
        encounterFormModel?.toEncounterForm()?.let { encounterForm ->
            encounterModel?.firstOrNull()?.toEncounter()?.let { encounter ->
                photoModel?.firstOrNull()?.toPhoto()?.let { photo ->
                    return EncounterFormWithEncounterAndPhoto(encounterForm, encounter, photo)
                }
                throw IllegalStateException("PhotoModel cannot be null")
            }
            throw IllegalStateException("EncounterModel cannot be null")
        }
        throw IllegalStateException("EncounterFormModel cannot be null")
    }
}
