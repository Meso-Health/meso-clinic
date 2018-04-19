package org.watsi.device.db.repositories

import org.threeten.bp.Clock
import org.watsi.device.db.daos.MemberDao
import org.watsi.device.db.models.MemberModel
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.Member
import org.watsi.domain.repositories.MemberRepository
import java.util.UUID

class MemberRepositoryImpl(private val memberDao: MemberDao,
                           private val clock: Clock) : MemberRepository {

    override fun find(id: UUID): Member {
        return memberDao.find(id).toMember()
    }

    override fun save(member: Member) {
        return memberDao.insert(MemberModel.fromMember(member, clock))
    }

    override fun destroy(member: Member) {
        memberDao.destroy(MemberModel.fromMember(member, clock))
    }

    override fun updateFromFetch(member: Member) {
        // TODO: implement
    }

    override fun findByCardId(cardId: String): Member {
        return memberDao.findByCardId(cardId).toMember()
    }

    override fun fuzzySearchByCardId(query: String): List<Member> {
        // TODO: implement
        return emptyList()
    }

    override fun fuzzySearchByName(query: String): List<Member> {
        // TODO: implement
        return emptyList()
    }

    override fun checkedInMembers(): List<Member> {
        return memberDao.checkedInMembers().map { it.toMember() }
    }

    override fun remainingHouseholdMembers(householdId: UUID, memberId: UUID): List<Member> {
        return memberDao.remainingHouseholdMembers(householdId, memberId).map { it.toMember() }
    }

    override fun membersWithPhotosToFetch(): List<Member> {
        // TODO: implement
        return emptyList()
    }

    override fun allIds(): Set<UUID> {
        return memberDao.allIds()
    }

    override fun sync(deltas: List<Delta>) {
        // TODO: implement
    }
}
