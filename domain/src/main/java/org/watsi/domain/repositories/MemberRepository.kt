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
    fun all(excludeArchived: Boolean = true): Flowable<List<Member>>
    fun find(id: UUID): Maybe<Member>
    fun findAll(ids: List<UUID>): Single<List<Member>>
    fun findMemberWithThumbnailFlowable(id: UUID): Flowable<MemberWithThumbnail>
    fun findByCardId(cardId: String, excludeArchived: Boolean = true): Maybe<Member>
    fun findHouseholdIdByMembershipNumber(cardId: String, excludeArchived: Boolean = true): Maybe<UUID>
    fun findHouseholdIdByCardId(cardId: String, excludeArchived: Boolean = true): Maybe<UUID>
    fun byIds(ids: List<UUID>): Single<List<MemberWithIdEventAndThumbnailPhoto>>
    fun checkedInMembers(): Flowable<List<MemberWithIdEventAndThumbnailPhoto>>
    fun isMemberCheckedIn(memberId: UUID): Flowable<Boolean>
    fun findHouseholdMembers(householdId: UUID, excludeArchived: Boolean = true): Flowable<List<MemberWithIdEventAndThumbnailPhoto>>
    fun upsert(member: Member, deltas: List<Delta>): Completable
    fun fetch(): Completable
    fun downloadPhotos(): Completable
    fun withPhotosToFetchCount(): Flowable<Int>
    fun sync(deltas: List<Delta>): Completable
    fun syncPhotos(deltas: List<Delta>): Completable
}
