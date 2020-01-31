package org.watsi.domain.usecases

import io.reactivex.Flowable
import org.watsi.domain.entities.Billable
import org.watsi.domain.repositories.BillableRepository

class LoadAllBillablesTypesUseCase(private val billableRepository: BillableRepository) {
    fun execute(): Flowable<List<Billable.Type>> {
        return billableRepository.uniqueTypes()
    }
}
