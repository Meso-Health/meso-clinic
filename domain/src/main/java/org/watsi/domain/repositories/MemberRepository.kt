package org.watsi.domain.repositories

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.Member
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.domain.relations.MemberWithThumbnail
import java.util.UUID

interface MemberRepository {
    fun all(): Flowable<List<Member>>
    fun find(id: UUID): Flowable<Member>
    fun findMemberWithThumbnailFlowable(id: UUID): Flowable<MemberWithThumbnail>
    fun findByCardId(cardId: String): Maybe<Member>
    fun byIds(ids: List<UUID>): Single<List<MemberWithIdEventAndThumbnailPhoto>>
    fun checkedInMembers(): Flowable<List<MemberWithIdEventAndThumbnailPhoto>>
    fun remainingHouseholdMembers(member: Member): Flowable<List<MemberWithThumbnail>>
    fun save(member: Member, deltas: List<Delta>): Completable
    fun fetch(): Completable
    fun downloadPhotos(): Completable
    fun withPhotosToFetchCount(): Flowable<Int>
    fun sync(deltas: List<Delta>): Completable
    fun syncPhotos(deltas: List<Delta>): Completable
}
