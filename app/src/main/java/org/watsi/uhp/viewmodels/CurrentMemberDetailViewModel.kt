package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable
import org.watsi.domain.entities.Billable
import io.reactivex.Flowable
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterItem
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.entities.Member
import org.watsi.domain.entities.Photo
import org.watsi.domain.relations.EncounterItemWithBillable
import org.watsi.domain.relations.EncounterWithItemsAndForms
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.domain.relations.MemberWithThumbnail
import org.watsi.domain.usecases.DismissIdentificationEventUseCase
import org.watsi.domain.usecases.LoadDefaultBillablesUseCase
import org.watsi.domain.usecases.LoadHouseholdMembersUseCase
import org.watsi.domain.usecases.LoadMemberUseCase
import java.util.UUID
import javax.inject.Inject

class CurrentMemberDetailViewModel @Inject constructor(
        private val loadMemberUseCase: LoadMemberUseCase,
        private val loadDefaultOpdBillables: LoadDefaultBillablesUseCase,
        private val loadHouseholdMembersUseCase: LoadHouseholdMembersUseCase,
        private val dismissIdentificationEventUseCase: DismissIdentificationEventUseCase,
        private val clock: Clock,
        private val logger: Logger
) : ViewModel() {

    private val defaultBillables: MutableList<Billable> = mutableListOf()

    fun getObservable(member: Member, identificationEvent: IdentificationEvent): LiveData<ViewState> {
        loadDefaultOpdBillables.execute(identificationEvent).subscribe({
            defaultBillables.clear()
            defaultBillables.addAll(it)
        }, {
            // TODO: handle error
        })

        val flowables = listOf(
                loadMemberUseCase.execute(member.id),
                loadHouseholdMembersUseCase.execute(member)
        )

        val zippedFlowables = Flowable.zip(flowables, {results ->
            val memberWithThumbnail = results[0] as MemberWithThumbnail
            ViewState(
                    member = memberWithThumbnail.member,
                    memberThumbnail = memberWithThumbnail.photo,
                    householdMembers = results[1] as List<MemberWithIdEventAndThumbnailPhoto>)
        }).onErrorReturn {
            logger.error(it)
            ViewState(null, null, null)
        }.startWith(
            ViewState(member)
        )
        return LiveDataReactiveStreams.fromPublisher(zippedFlowables)
    }

    fun dismiss(identificationEvent: IdentificationEvent): Completable {
        return dismissIdentificationEventUseCase.execute(identificationEvent)
    }

    fun buildEncounter(idEvent: IdentificationEvent): EncounterWithItemsAndForms {
        val encounterId = UUID.randomUUID()
        val defaultEncounterItems = defaultBillables.map {
            val encounterItem = EncounterItem(UUID.randomUUID(), encounterId, it.id, 1)
            EncounterItemWithBillable(encounterItem, it)
        }
        val encounter = Encounter(encounterId, idEvent.memberId, idEvent.id, Instant.now(clock), null)
        return EncounterWithItemsAndForms(encounter, defaultEncounterItems, emptyList())
    }

    data class ViewState(val member: Member?,
                         val memberThumbnail: Photo? = null,
                         val householdMembers: List<MemberWithIdEventAndThumbnailPhoto>? = null)
}
