package org.watsi.domain.usecases

import io.reactivex.Single
import org.watsi.domain.entities.Billable
import org.watsi.domain.relations.BillableWithPriceSchedule
import org.watsi.domain.repositories.BillableRepository

class LoadBillablesOfTypeWithPriceUseCase(private val billableRepository: BillableRepository) {
    fun execute(type: Billable.Type): Single<List<BillableWithPriceSchedule>> {
        return billableRepository.ofTypeWithPrice(type)
    }
}
