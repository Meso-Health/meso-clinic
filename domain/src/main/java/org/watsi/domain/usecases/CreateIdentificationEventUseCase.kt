package org.watsi.domain.usecases

import io.reactivex.Completable
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.repositories.IdentificationEventRepository

class CreateIdentificationEventUseCase(private val identificationEventRepo: IdentificationEventRepository) {

    fun execute(identificationEvent: IdentificationEvent): Completable {
        return identificationEventRepo.create(identificationEvent, Delta(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.IDENTIFICATION_EVENT,
                modelId = identificationEvent.id
        ))
    }
}
