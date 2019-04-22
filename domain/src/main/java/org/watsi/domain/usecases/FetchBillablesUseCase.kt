package org.watsi.domain.usecases

import io.reactivex.Completable
import org.watsi.domain.repositories.BillableRepository

class FetchBillablesUseCase(private val billableRepository: BillableRepository) {

    fun execute(): Completable {
        return billableRepository.fetch()
    }
}
