package org.watsi.domain.usecases

import io.reactivex.Single
import org.watsi.domain.relations.BillableWithPriceSchedule
import org.watsi.domain.repositories.BillableRepository

class LoadAllBillablesUseCase(private val billableRepository: BillableRepository) {
    fun execute(): Single<List<BillableWithPriceSchedule>> {
        return billableRepository.all()
    }
}
