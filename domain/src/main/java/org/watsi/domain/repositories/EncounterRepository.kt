package org.watsi.domain.repositories

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import org.threeten.bp.Instant
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.Encounter
import org.watsi.domain.relations.EncounterWithExtras
import java.util.UUID

interface EncounterRepository {
    fun find(id: UUID): Single<EncounterWithExtras>
    fun findAll(ids: List<UUID>): Single<List<Encounter>>
    fun findAllWithExtras(ids: List<UUID>): Single<List<EncounterWithExtras>>
    fun update(encounters: List<Encounter>): Completable
    fun insert(encounterWithExtras: EncounterWithExtras, deltas: List<Delta>): Completable
    fun upsert(encounterWithExtras: EncounterWithExtras): Completable
    fun upsert(encounters: List<EncounterWithExtras>): Completable
    fun delete(encounterId: UUID): Completable
    fun deleteEncounterItems(ids: List<UUID>): Completable
    fun sync(delta: Delta): Completable
    fun fetchReturnedClaims(): Single<List<EncounterWithExtras>>
    fun loadPendingClaimsCount(): Flowable<Int>
    fun loadPendingClaims(): Flowable<List<EncounterWithExtras>>
    fun loadOnePendingClaim(): Maybe<EncounterWithExtras>
    fun loadOneReturnedClaim(): Maybe<EncounterWithExtras>
    fun loadReturnedClaimsCount(): Flowable<Int>
    fun loadReturnedClaims(): Flowable<List<EncounterWithExtras>>
    fun returnedIds(): Single<List<UUID>>
    fun revisedIds(): Single<List<UUID>>
    fun encountersOccurredSameDay(occurredAt: Instant, memberId: UUID): Single<Boolean>
}
