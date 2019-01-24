package org.watsi.domain.relations

import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.entities.Member
import org.watsi.domain.entities.Member.RelationshipToHead
import org.watsi.domain.entities.Photo

data class MemberWithIdEventAndThumbnailPhoto(val member: Member,
                                              val identificationEvent: IdentificationEvent?,
                                              val thumbnailPhoto: Photo?) {
    companion object {
        fun asSortedListWithHeadOfHouseholdsFirst(members: List<MemberWithIdEventAndThumbnailPhoto>): List<MemberWithIdEventAndThumbnailPhoto> {
            val headsOfHousehold = members
                .filter { it.member.relationshipToHead == RelationshipToHead.SELF }
                .sortedBy { it.member.enrolledAt }
            val beneficiaries = (members - headsOfHousehold).sortedBy { it.member.enrolledAt }
            return headsOfHousehold + beneficiaries
        }
    }
}
