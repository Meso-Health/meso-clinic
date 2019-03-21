package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterItem
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.entities.Member
import org.watsi.domain.entities.Photo
import org.watsi.domain.relations.BillableWithPriceSchedule
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice
import org.watsi.domain.repositories.IdentificationEventRepository
import org.watsi.domain.usecases.LoadDefaultBillablesUseCase
import org.watsi.domain.usecases.LoadMemberUseCase
import org.watsi.uhp.flowstates.EncounterFlowState
import java.util.UUID
import javax.inject.Inject

class CurrentMemberDetailViewModel @Inject constructor(
    private val loadMemberUseCase: LoadMemberUseCase,
    private val loadDefaultOpdBillables: LoadDefaultBillablesUseCase,
    private val identificationEventRepository: IdentificationEventRepository,
    private val clock: Clock,
    private val logger: Logger
) : ViewModel() {

    private val defaultBillables: MutableList<BillableWithPriceSchedule> = mutableListOf()

    fun getObservable(member: Member, identificationEvent: IdentificationEvent): LiveData<ViewState> {
        loadDefaultOpdBillables.execute(identificationEvent).subscribe({
            defaultBillables.clear()
            defaultBillables.addAll(it)
        }, {
            logger.error(it)
        })

        val flowable = loadMemberUseCase.execute(member.id)
                .map { ViewState(member = it.member, memberThumbnail = it.photo) }
                .onErrorReturn {
                    logger.error(it)
                    ViewState(null)
                }
        return LiveDataReactiveStreams.fromPublisher(flowable)
    }

    fun dismiss(identificationEvent: IdentificationEvent): Completable {
        return identificationEventRepository.dismiss(identificationEvent)
    }

    fun buildEncounter(idEvent: IdentificationEvent, member: Member): EncounterFlowState {
        val encounterId = UUID.randomUUID()
        val defaultEncounterItems = defaultBillables.map {
            val encounterItem = EncounterItem(
                id = UUID.randomUUID(),
                encounterId = encounterId,
                quantity = 1,
                priceScheduleId = it.priceSchedule.id,
                priceScheduleIssued = false
            )
            EncounterItemWithBillableAndPrice(encounterItem, it)
        }
        val encounter = Encounter(encounterId, idEvent.memberId, idEvent.id, Instant.now(clock), Instant.now(clock))
        return EncounterFlowState(
            encounter = encounter,
            encounterItemRelations = defaultEncounterItems,
            encounterForms = emptyList(),
            diagnoses = emptyList(),
            member = member,
            referral = null
        )
    }

    data class ViewState(val member: Member?,
                         val memberThumbnail: Photo? = null)
}
