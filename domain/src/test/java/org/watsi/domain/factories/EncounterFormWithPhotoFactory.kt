package org.watsi.domain.factories

import org.watsi.domain.entities.EncounterForm
import org.watsi.domain.entities.Photo
import org.watsi.domain.relations.EncounterFormWithPhoto

object EncounterFormWithPhotoFactory {
    fun build(photo: Photo = PhotoFactory.build(),
              encounterForm: EncounterForm = EncounterFormFactory.build(photoId = photo.id)
    ) : EncounterFormWithPhoto {
        return EncounterFormWithPhoto(encounterForm, photo)
    }
}
