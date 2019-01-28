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
    fun all(filterArchived: Boolean = true): Flowable<List<Member>>
    fun find(id: UUID): Single<Member>
    fun findMemberWithThumbnailFlowable(id: UUID): Flowable<MemberWithThumbnail>
    fun findByCardId(cardId: String, filterArchived: Boolean = true): Maybe<Member>
    fun findHouseholdIdByMembershipNumber(cardId: String, filterArchived: Boolean = true): Maybe<UUID>
    fun findHouseholdIdByCardId(cardId: String, filterArchived: Boolean = true): Maybe<UUID>
    fun byIds(ids: List<UUID>): Single<List<MemberWithIdEventAndThumbnailPhoto>>
    fun checkedInMembers(filterArchived: Boolean = true): Flowable<List<MemberWithIdEventAndThumbnailPhoto>>
    fun isMemberCheckedIn(memberId: UUID): Flowable<Boolean>
    fun findHouseholdMembers(householdId: UUID, filterArchived: Boolean = true): Flowable<List<MemberWithIdEventAndThumbnailPhoto>>
    fun upsert(member: Member, deltas: List<Delta>): Completable
    fun fetch(): Completable
    fun downloadPhotos(): Completable
    fun withPhotosToFetchCount(): Flowable<Int>
    fun sync(deltas: List<Delta>): Completable
    fun syncPhotos(deltas: List<Delta>): Completable
}
