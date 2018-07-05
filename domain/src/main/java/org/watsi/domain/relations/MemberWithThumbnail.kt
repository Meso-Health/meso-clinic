package org.watsi.domain.relations

import org.watsi.domain.entities.Member
import org.watsi.domain.entities.Photo

data class MemberWithThumbnail(val member: Member, val photo: Photo?)