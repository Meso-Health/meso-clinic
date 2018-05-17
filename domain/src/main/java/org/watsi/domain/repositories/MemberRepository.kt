package org.watsi.domain.repositories

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.Member
import java.util.UUID

interface MemberRepository {
    fun all(): Flowable<List<Member>>
    fun find(id: UUID): Flowable<Member>
    fun save(member: Member, deltas: List<Delta>): Completable
    fun fetch(): Completable
    fun findByCardId(cardId: String): Maybe<Member>
    fun checkedInMembers(): Flowable<List<Member>>
    fun remainingHouseholdMembers(member: Member): Flowable<List<Member>>
    fun sync(deltas: List<Delta>): Completable
    fun downloadPhotos(): Completable
    fun withPhotosToFetchCount(): Flowable<Int>
}
