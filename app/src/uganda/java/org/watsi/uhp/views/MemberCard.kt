package org.watsi.uhp.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import kotlinx.android.synthetic.uganda.view_member_card.view.clinic_number
import kotlinx.android.synthetic.uganda.view_member_card.view.member_age_and_gender
import kotlinx.android.synthetic.uganda.view_member_card.view.member_card_id
import kotlinx.android.synthetic.uganda.view_member_card.view.member_name
import kotlinx.android.synthetic.uganda.view_member_card.view.member_photo
import org.threeten.bp.Clock
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.entities.Member
import org.watsi.domain.entities.Member.Companion.formatCardId
import org.watsi.domain.entities.Photo
import org.watsi.uhp.R
import org.watsi.uhp.helpers.PhotoLoader

class MemberCard @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_member_card, this, true)
    }

    fun setMember(member: Member, thumbnail: Photo?, clock: Clock) {
        member_name.text = member.name
        member_age_and_gender.text = member.formatAgeAndGender(clock)
        member.cardId?.let { member_card_id.text = formatCardId(it) }
        PhotoLoader.loadMemberPhoto(
                thumbnail?.bytes, member_photo, context, member.gender)
    }

    fun setIdentificationEvent(identificationEvent: IdentificationEvent) {
        clinic_number.text = identificationEvent.formatClinicNumber()
    }
}
