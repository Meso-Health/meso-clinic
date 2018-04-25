package org.watsi.device.db.repositories

import org.threeten.bp.Clock
import org.watsi.device.api.CoverageApi
import org.watsi.device.db.daos.MemberDao
import org.watsi.device.db.models.MemberModel
import org.watsi.device.managers.PreferencesManager
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.Member
import org.watsi.domain.repositories.MemberRepository
import java.util.UUID

class MemberRepositoryImpl(private val memberDao: MemberDao,
                           private val api: CoverageApi,
                           private val sessionManager: SessionManager,
                           private val preferencesManager: PreferencesManager,
                           private val clock: Clock) : MemberRepository {

    override fun find(id: UUID): Member {
        return memberDao.find(id)!!.toMember()
    }

    override fun save(member: Member) {
        if (memberDao.find(member.id) != null) {
            memberDao.update(MemberModel.fromMember(member, clock))
        } else {
            memberDao.insert(MemberModel.fromMember(member, clock))
        }
    }

    override fun fetch() {
        sessionManager.currentToken()?.let { token ->
            api.members(token.getHeaderString(), token.user.providerId).execute()?.let { response ->
                // TODO: handle null body
                if (response.isSuccessful) {
                    response.body()?.let { updatedMembers ->
                        updatedMembers.forEach { save(it.toMember()) }
                        // TODO: more efficient way of saving?
                        // TODO: clean up any members not returned in the fetch
                        // TODO: do not overwrite unsynced member data
                    }
                    preferencesManager.updateMemberLastFetched(clock.instant())
                } else {
                    // TODO: log
                }
                // TODO: handle null response
            }
            // TODO: handle logged out case
        }
    }

    override fun findByCardId(cardId: String): Member? {
        return memberDao.findByCardId(cardId)?.toMember()
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

    override fun sync(deltas: List<Delta>) {
        // TODO: implement
    }
}
