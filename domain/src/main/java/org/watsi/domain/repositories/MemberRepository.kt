package org.watsi.domain.repositories

import io.reactivex.Flowable
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.Member
import java.util.UUID

interface MemberRepository {
    fun find(id: UUID): Member
    fun save(member: Member)
    fun fetch()
    fun findByCardId(cardId: String): Member?
    fun fuzzySearchByCardId(query: String): List<Member>
    fun fuzzySearchByName(query: String): List<Member>
    fun checkedInMembers(): Flowable<List<Member>>
    fun remainingHouseholdMembers(householdId: UUID, memberId: UUID): List<Member>
    fun membersWithPhotosToFetch(): List<Member>
    fun sync(deltas: List<Delta>)
}
