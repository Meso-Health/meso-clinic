package org.watsi.uhp.views

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import kotlinx.android.synthetic.main.item_member_list.view.member_age_and_gender
import kotlinx.android.synthetic.main.item_member_list.view.member_card_id
import kotlinx.android.synthetic.main.item_member_list.view.member_clinic_number
import kotlinx.android.synthetic.main.item_member_list.view.member_name
import kotlinx.android.synthetic.main.item_member_list.view.member_phone_number
import kotlinx.android.synthetic.main.item_member_list.view.member_photo
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.uhp.helpers.PhotoLoader

class MemberListItem @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {
    fun setMemberRelation(memberRelation: MemberWithIdEventAndThumbnailPhoto, showClinicNumber: Boolean, showPhoneNumber: Boolean) {
        memberRelation.member.let {member ->
            member_name.text = member.name
            member_age_and_gender.text = member.formatAgeAndGender()
            if (showPhoneNumber) {
                member_phone_number.visibility = View.VISIBLE
                member_phone_number.text = member.formattedPhoneNumber()
            }
            member_card_id.text = member.cardId
        }
        memberRelation.thumbnailPhoto?.bytes?.let { bytes->
            PhotoLoader.loadMemberPhoto(bytes, member_photo, context)
        }

        if (showClinicNumber) {
            memberRelation.identificationEvent?.let { identificationEvent ->
                member_clinic_number.visibility = View.VISIBLE
                member_clinic_number.text = identificationEvent.formatClinicNumber()
            }
        }
    }
}