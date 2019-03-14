package org.watsi.domain.usecases

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.watsi.domain.repositories.BillableRepository
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.EncounterFormRepository
import org.watsi.domain.repositories.EncounterRepository
import org.watsi.domain.repositories.IdentificationEventRepository
import org.watsi.domain.repositories.MemberRepository
import org.watsi.domain.repositories.PriceScheduleRepository

class DeleteUserDataUseCase(
    private val billableRepository: BillableRepository,
    private val deltaRepository: DeltaRepository,
    private val identificationEventRepository: IdentificationEventRepository,
    private val memberRepository: MemberRepository,
    private val priceScheduleRepository: PriceScheduleRepository,
    private val encounterRepository: EncounterRepository
//    private val encounterFormRepository: EncounterFormRepository,
//    private val encounterItemRepository: EncounterRepository
){
    fun execute(): Completable {
        return Completable.concatArray(
            deltaRepository.deleteAll(),
//            encounterItemRepository.deleteAll(),
//            encounterFormRepository.deleteAll(),
            encounterRepository.deleteAll(),
            priceScheduleRepository.deleteAll(),
            billableRepository.deleteAll(),
            identificationEventRepository.deleteAll(),
            memberRepository.deleteAll()
        ).subscribeOn(Schedulers.io())
    }
}
