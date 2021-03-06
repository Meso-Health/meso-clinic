package org.watsi.uhp.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import kotlinx.android.synthetic.uganda.view_member_list_item.view.member_description
import kotlinx.android.synthetic.uganda.view_member_list_item.view.member_photo
import kotlinx.android.synthetic.uganda.view_member_list_item.view.name
import org.threeten.bp.Clock
import org.watsi.domain.entities.Member
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.uhp.R
import org.watsi.uhp.helpers.PhotoLoader
import org.watsi.uhp.helpers.StringHelper

class MemberListItem @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val placeholderPadding = resources.getDimensionPixelSize(R.dimen.thumbnailSmallPlaceholderPadding)

    fun setMember(memberRelation: MemberWithIdEventAndThumbnailPhoto) {
        val member = memberRelation.member

        // set member photo and apply padding if using the placeholder image
        val padding = if (memberRelation.thumbnailPhoto == null) placeholderPadding else 0
        member_photo.setPadding(padding, padding, padding, padding)
        PhotoLoader.loadMemberPhoto(
            bytes = memberRelation.thumbnailPhoto?.bytes,
            view = member_photo,
            context = context,
            gender = member.gender,
            photoExists = member.photoExists()
        )

        name.text = member.name

        val genderString = if (member.gender == Member.Gender.F) {
            context.getString(R.string.female)
        } else {
            context.getString(R.string.male)
        }

        val description = memberRelation.identificationEvent?.let {
            context.getString(
                R.string.member_list_item_description_checked_in,
                it.formatClinicNumber(),
                genderString,
                StringHelper.getDisplayAge(member, context)
            )
        } ?: run {
            context.getString(
                R.string.member_list_item_description,
                genderString,
                StringHelper.getDisplayAge(member, context)
            )
        }

        member_description.text = description
    }
}
