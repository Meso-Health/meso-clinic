package org.watsi.domain.usecases

import io.reactivex.Completable
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.repositories.IdentificationEventRepository

class DismissIdentificationEventUseCase(private val identificationEventRepo: IdentificationEventRepository) {

    fun execute(identificationEvent: IdentificationEvent): Completable {
        return identificationEventRepo.dismiss(identificationEvent, Delta(
                action = Delta.Action.EDIT,
                modelName = Delta.ModelName.IDENTIFICATION_EVENT,
                modelId = identificationEvent.id,
                field = "dismissed"))
    }
}
