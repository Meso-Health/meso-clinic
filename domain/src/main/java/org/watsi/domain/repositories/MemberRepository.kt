package org.watsi.domain.repositories

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.Member
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import java.util.UUID

interface MemberRepository {
    fun all(): Flowable<List<Member>>
    fun find(id: UUID): Flowable<Member>
    fun create(member: Member, deltas: List<Delta>): Completable
    fun update(member: Member, deltas: List<Delta>): Completable
    fun fetch(): Completable
    fun findByCardId(cardId: String): Maybe<Member>
    fun byIds(ids: List<UUID>): Single<List<MemberWithIdEventAndThumbnailPhoto>>
    fun checkedInMembers(): Flowable<List<MemberWithIdEventAndThumbnailPhoto>>
    fun remainingHouseholdMembers(member: Member): Flowable<List<Member>>
    fun sync(deltas: List<Delta>): Completable
    fun downloadPhotos(): Completable
    fun withPhotosToFetchCount(): Flowable<Int>
}
