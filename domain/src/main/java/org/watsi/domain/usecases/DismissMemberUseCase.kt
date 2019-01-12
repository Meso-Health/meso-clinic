package org.watsi.domain.usecases

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.watsi.domain.repositories.IdentificationEventRepository
import java.util.UUID

class DismissMemberUseCase(
    private val identificationEventRepository: IdentificationEventRepository
) {
    fun execute(memberId: UUID): Completable {
        return Completable.fromAction {
            val idEvent = identificationEventRepository.openCheckIn(memberId).blockingGet()
            identificationEventRepository.dismiss(idEvent).blockingAwait()
        }.subscribeOn(Schedulers.io())
    }
}
