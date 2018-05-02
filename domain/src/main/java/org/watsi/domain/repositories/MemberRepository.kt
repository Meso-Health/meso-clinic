package org.watsi.domain.repositories

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.Member
import java.util.UUID

interface MemberRepository {
    fun all(): Flowable<List<Member>>
    fun find(id: UUID): Flowable<Member>
    fun save(member: Member): Completable
    fun fetch(): Completable
    fun findByCardId(cardId: String): Maybe<Member>
    fun checkedInMembers(): Flowable<List<Member>>
    fun remainingHouseholdMembers(householdId: UUID, memberId: UUID): Flowable<List<Member>>
    fun sync(deltas: List<Delta>): Completable
    fun downloadPhotos(): Completable
}
