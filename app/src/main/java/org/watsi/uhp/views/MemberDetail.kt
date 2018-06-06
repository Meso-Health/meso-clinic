package org.watsi.uhp.views

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import kotlinx.android.synthetic.main.view_member_detail.view.member_age_and_gender
import kotlinx.android.synthetic.main.view_member_detail.view.member_card_id
import kotlinx.android.synthetic.main.view_member_detail.view.member_clinic_number
import kotlinx.android.synthetic.main.view_member_detail.view.member_name
import kotlinx.android.synthetic.main.view_member_detail.view.member_phone_number
import kotlinx.android.synthetic.main.view_member_detail.view.member_photo
import org.threeten.bp.Clock
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.uhp.helpers.PhotoLoader

class MemberDetail @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {
    fun setMemberRelation(memberRelation: MemberWithIdEventAndThumbnailPhoto, clock: Clock) {
        memberRelation.member.let {member ->
            member_name.text = member.name
            member_age_and_gender.text = member.formatAgeAndGender(clock)
            memberRelation.member.phoneNumber?.let {
                member_phone_number.visibility = View.VISIBLE
                member_phone_number.text = member.formattedPhoneNumber()
            }
            member_card_id.text = member.cardId

            PhotoLoader.loadMemberPhoto(
                    memberRelation.thumbnailPhoto?.bytes, member_photo, context, member.gender)
        }

        memberRelation.identificationEvent?.let { identificationEvent ->
            member_clinic_number.visibility = View.VISIBLE
            member_clinic_number.text = identificationEvent.formatClinicNumber()
        }
    }
}
