package org.watsi.domain.relations

import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.entities.Member
import org.watsi.domain.entities.Photo

data class MemberWithIdEventAndThumbnailPhoto(val member: Member,
                                              val identificationEvent: IdentificationEvent?,
                                              val thumbnailPhoto: Photo?)
