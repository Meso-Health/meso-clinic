package org.watsi.uhp.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import kotlinx.android.synthetic.ethiopia.view_member_list_item.view.gender_age
import kotlinx.android.synthetic.ethiopia.view_member_list_item.view.member_tag
import kotlinx.android.synthetic.ethiopia.view_member_list_item.view.name
import kotlinx.android.synthetic.ethiopia.view_member_list_item.view.photo_container
import org.threeten.bp.Clock
import org.watsi.domain.entities.Member
import org.watsi.domain.entities.Member.Gender
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.uhp.R
import org.watsi.uhp.helpers.PhotoLoader
import org.watsi.uhp.helpers.StringHelper

class MemberListItem @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val placeholderPhotoIconPadding = resources.getDimensionPixelSize(R.dimen.thumbnailSmallPlaceholderPadding)

    fun setMember(memberWithThumbnail: MemberWithIdEventAndThumbnailPhoto) {
        val member = memberWithThumbnail.member

        name.text = member.name
        val genderString = if (member.gender == Gender.F) {
            resources.getString(R.string.female)
        } else {
            resources.getString(R.string.male)
        }
        gender_age.text = resources.getString(R.string.member_list_item_gender_age,
            genderString,
            StringHelper.getDisplayAge(member, context))

        if (member.relationshipToHead == Member.RelationshipToHead.SELF) {
            member_tag.visibility = View.VISIBLE
        }

        PhotoLoader.loadMemberPhoto(
            memberWithThumbnail.thumbnailPhoto?.bytes,
            photo_container,
            context,
            member.gender,
            placeholderPhotoIconPadding
        )
    }
}
