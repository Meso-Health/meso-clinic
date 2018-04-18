package org.watsi.uhp.repositories

import org.watsi.uhp.database.MemberDao
import org.watsi.uhp.models.Member
import java.util.UUID

class MemberRepositoryImpl : MemberRepository {
    override fun findByCardId(cardId: String): Member? {
        return MemberDao.findByCardId(cardId)
    }

    override fun fuzzySearchByCardId(query: String): List<Member> {
        return MemberDao.withCardIdLike(query)
    }

    override fun fuzzySearchByName(query: String): List<Member> {
        return MemberDao.fuzzySearchMembers(query)
    }

    override fun checkedInMembers(): List<Member> {
        return MemberDao.getCheckedInMembers()
    }

    override fun remainingHouseholdMembers(householdId: UUID, memberId: UUID): List<Member> {
        return MemberDao.getRemainingHouseholdMembers(householdId, memberId)
    }

    override fun membersWithPhotosToFetch(): List<Member> {
        return MemberDao.membersWithPhotosToFetch()
    }

    override fun allIds(): Set<UUID> {
        return MemberDao.allMemberIds()
    }
}
