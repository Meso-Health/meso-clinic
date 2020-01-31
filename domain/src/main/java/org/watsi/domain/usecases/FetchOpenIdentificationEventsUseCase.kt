package org.watsi.domain.usecases

import io.reactivex.Completable
import org.watsi.domain.repositories.IdentificationEventRepository

class FetchOpenIdentificationEventsUseCase(private val identificationEventRepository: IdentificationEventRepository) {

    fun execute(): Completable {
        return identificationEventRepository.fetch()
    }
}
