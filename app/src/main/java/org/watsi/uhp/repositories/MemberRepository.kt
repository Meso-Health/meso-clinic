package org.watsi.uhp.repositories

import org.watsi.uhp.models.Member
import java.util.UUID

interface MemberRepository {
    fun findByCardId(cardId: String): Member?
    fun fuzzySearchByCardId(query: String): List<Member>
    fun fuzzySearchByName(query: String): List<Member>
    fun checkedInMembers(): List<Member>
    fun remainingHouseholdMembers(householdId: UUID, memberId: UUID): List<Member>
    fun membersWithPhotosToFetch(): List<Member>
    fun allIds(): Set<UUID>
}
