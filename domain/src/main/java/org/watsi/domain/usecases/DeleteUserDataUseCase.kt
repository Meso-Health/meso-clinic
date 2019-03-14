package org.watsi.domain.usecases

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.watsi.domain.repositories.BillableRepository
import org.watsi.domain.repositories.IdentificationEventRepository
import org.watsi.domain.repositories.MemberRepository
import org.watsi.domain.repositories.PriceScheduleRepository

class DeleteUserDataUseCase(
    private val billableRepository: BillableRepository,
    private val identificationEventRepository: IdentificationEventRepository,
    private val memberRepository: MemberRepository,
    private val priceScheduleRepository: PriceScheduleRepository
){
    fun execute(): Completable {
        return Completable.concatArray(
            billableRepository.deleteAll(),
            identificationEventRepository.deleteAll(),
            memberRepository.deleteAll(),
            priceScheduleRepository.deleteAll()
        ).subscribeOn(Schedulers.io())
    }
}
