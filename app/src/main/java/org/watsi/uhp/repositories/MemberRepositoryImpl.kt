package org.watsi.uhp.repositories

import org.watsi.domain.entities.Delta
import org.watsi.uhp.database.DatabaseHelper
import org.watsi.uhp.database.MemberDao
import org.watsi.uhp.models.Member
import java.util.UUID

class MemberRepositoryImpl : MemberRepository {

    override fun find(id: UUID): Member? {
        return DatabaseHelper.fetchDao(Member::class.java).queryForId(id) as Member?
    }

    override fun save(member: Member) {
        // TODO: set token, validate, set ID (if necessary) and set dirty fields
        DatabaseHelper.fetchDao(Member::class.java).createOrUpdate(member)
    }

    override fun refresh(member: Member) {
        DatabaseHelper.fetchDao(Member::class.java).refresh(member)
    }

    override fun destroy(member: Member) {
        DatabaseHelper.fetchDao(Member::class.java).delete(member)
    }

    override fun updateFromFetch(member: Member) {
        val persistedMember = find(member.id)
        if (persistedMember != null) {
            // if the persisted member has not been synced to the back-end, assume it is
            // the most up-to-date and do not update it with the fetched member attributes
            // TODO: re-implement the isSynced check
            //            if (!persistedMember.isSynced()) {
            //                return;
            //            }

            // if the existing member record has a photo and the fetched member record has
            // the same photo url as the existing record, copy the photo to the new record
            // so we do not have to re-download it
            if (persistedMember.croppedPhotoBytes != null &&
                    persistedMember.remoteMemberPhotoUrl != null &&
                    persistedMember.remoteMemberPhotoUrl == member.remoteMemberPhotoUrl) {
                member.croppedPhotoBytes = persistedMember.croppedPhotoBytes
            }

            if (member.remoteMemberPhotoUrl != null && member.remoteMemberPhotoUrl !=
                    persistedMember.remoteMemberPhotoUrl) {
                member.croppedPhotoBytes = null
            }
        }
        save(member)
    }

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

    override fun sync(deltas: List<Delta>) {
        // TODO
    }
}
