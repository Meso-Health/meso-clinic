package org.watsi.domain.relations

import org.watsi.domain.entities.Member
import org.watsi.domain.entities.Photo

data class MemberWithRawPhoto(val member: Member, val photo: Photo)
