package org.watsi.uhp.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import kotlinx.android.synthetic.ethiopia.view_member_card.view.age_and_gender
import kotlinx.android.synthetic.ethiopia.view_member_card.view.member_photo
import kotlinx.android.synthetic.ethiopia.view_member_card.view.membership_info
import kotlinx.android.synthetic.ethiopia.view_member_card.view.name
import org.threeten.bp.Clock
import org.watsi.domain.entities.Member
import org.watsi.domain.entities.Photo
import org.watsi.uhp.R
import org.watsi.uhp.helpers.MemberStringHelper
import org.watsi.uhp.helpers.PhotoLoader

class MemberCard @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_member_card, this, true)
    }

    fun setMember(member: Member, thumbnail: Photo?, clock: Clock) {
        name.text = member.name
        age_and_gender.text = MemberStringHelper.formatAgeAndGender(member, context, clock)
        membership_info.text = MemberStringHelper.formatMembershipInfo(member, context)
        PhotoLoader.loadMemberPhoto(thumbnail?.bytes, member_photo, context, member.gender)
    }
}
